const functions = require('firebase-functions');

// The Firebase Admin SDK to access Cloud Firestore.
const admin = require('firebase-admin');
admin.initializeApp();


exports.paymentCallback = functions.https.onRequest(async (req, res) => {
    // Get the stk response body
    const callbackData = req.body.Body.stkCallback;

    console.log("Received payload: ", callbackData);

    const responseCode = callbackData.ResultCode;
    const mCheckoutRequestID = callbackData.CheckoutRequestID;

    if (responseCode === 0) {
        const details = callbackData.CallbackMetadata.Item

        var mReceipt;
        var mPhonePaidFrom;
        var mAmountPaid;

        await details.forEach(entry => {
            switch (entry.Name) {
                case "MpesaReceiptNumber":
                    mReceipt = entry.Value
                    break;

                case "PhoneNumber":
                    mPhonePaidFrom = entry.Value
                    break;

                case "Amount":
                    mAmountPaid = entry.Value;
                    break;

                default:
                    break;
            }
        })

        const mEntryDetails = {
            "receipt": mReceipt,
            "phone": mPhonePaidFrom,
            "amount": mAmountPaid
        }

        // Find the document initialized from client device containing the CheckoutRequestID.

        var matchingCheckoutID = admin.firestore().collectionGroup('deposit')
            .where('CheckoutRequestID', '==', mCheckoutRequestID);

        const queryResults = await matchingCheckoutID.get();

        if (!queryResults.empty) {
            // Case the match is found
            var documentMatchingID = queryResults.docs[0];
            //update account balance - first get the mail for particular user
            const mail = documentMatchingID.ref.path.split('/')[1]

            documentMatchingID.ref.update(mEntryDetails);

            admin.firestore().collection('payments')
                .doc(mail).collection('balance')
                .doc('account').get().then(async (account) => {

                    if (account.data() !== undefined) {
                        // When it's not the first time
                        var balance = account.data().wallet
                        const newBalance = balance + mAmountPaid
                        console.log("Account found updating with balance ", newBalance, " from ", balance);

                        return account.ref.update({ wallet: newBalance })
                    } else {
                        console.log("No account found...creating with new balance ", mAmountPaid);
                        //create fresh wallet
                        try {
                            return admin.firestore().collection('payments')
                                .doc(mail).collection('balance')
                                .doc('account').set({ wallet: mAmountPaid });
                        } catch (err) {
                            console.log("Error creating account when not found ", err);
                            return 1;
                        }
                    }
                }).catch((exc) => {
                    console.log("Exception getting account ", exc);
                    return { "data": exc }
                })

            console.log("Updated document: ", documentMatchingID.ref.path);

        } else {
            console.log("No document found matching the checkoutRequestID : ", mCheckoutRequestID);
            //Persist the data somewhere for reference.
            admin.firestore().doc('lost_found_receipts/deposit_info/all/'
             + mCheckoutRequestID).set(mEntryDetails);

        }

    } else {
        console.log("Failed transaction.");
        //You may wish to persist the data sent as well here.
    }

    // Send back a message that we've succesfully written the message
    res.json({ 'result': `Payment for ${mCheckoutRequestID} response received.` });

});
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:mpesa_flutter_plugin/initializer.dart';
import 'package:mpesa_flutter_plugin/payment_enums.dart';
import 'package:mpesademo1/keys.dart';

void main() {
  MpesaFlutterPlugin.setConsumerKey(kConsumerKey);
  MpesaFlutterPlugin.setConsumerSecret(kConsumerSecret);

  runApp(MyHomePage());
}

class MyHomePage extends StatefulWidget {
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  DocumentReference paymentsRef;
  String mUserMail = "bob@keron.co.ke";

  // Set default `_initialized` and `_error` state to false
  bool _initialized = false;
  bool _error = false;

  Future<void> updateAccount(String mCheckoutRequestID) {
    Map<String, String> initData = {
      'CheckoutRequestID': mCheckoutRequestID,
    };

    paymentsRef.set({"info": "$mUserMail receipts data goes here."});

    return paymentsRef
        .collection("deposit")
        .doc(mCheckoutRequestID)
        .set(initData)
        .then((value) => print("Transaction Initialized."))
        .catchError((error) => print("Failed to init transaction: $error"));
  }

  Stream<DocumentSnapshot> getAccountBalance() {
    if (_initialized) {
      return paymentsRef.collection("balance").doc("account").snapshots();
    } else {
      return null;
    }
  }

  Future<dynamic> startTransaction({double amount, String phone}) async {
    dynamic transactionInitialisation;
    //Wrap it with a try-catch
    try {
      //Run it
      transactionInitialisation = await MpesaFlutterPlugin.initializeMpesaSTKPush(
          businessShortCode: "174379",
          transactionType: TransactionType.CustomerPayBillOnline,
          amount: amount,
          partyA: phone,
          partyB: "174379",
          callBackURL: Uri(
              scheme: "https",
              host: "us-central1-nigel-da5d1.cloudfunctions.net",
              path: "paymentCallback"),
          accountReference: "she",
          phoneNumber: phone,
          baseUri: Uri(scheme: "https", host: "sandbox.safaricom.co.ke"),
          transactionDesc: "purc",
          passKey:
              "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919");

      var result = transactionInitialisation as Map<String, dynamic>;

      if (result.keys.contains("ResponseCode")) {
        String mResponseCode = result["ResponseCode"];
        print("Resulting Code: " + mResponseCode);
        if (mResponseCode == '0') {
          updateAccount(result["CheckoutRequestID"]);
        }
      }
      print("RESULT: " + transactionInitialisation.toString());
    } catch (e) {
      //you can implement your exception handling here.
      //Network unreachability is a sure exception.
      print("Exception Caught: " + e.toString());
    }
  }

  // Define an async function to initialize FlutterFire
  void initializeFlutterFire() async {
    try {
      // Wait for Firebase to initialize and set `_initialized` state to true
      await Firebase.initializeApp();
      setState(() {
        _initialized = true;
      });

      paymentsRef =
          FirebaseFirestore.instance.collection('payments').doc(mUserMail);
    } catch (e) {
      print(e.toString());
      // Set `_error` state to true if Firebase initialization fails
      setState(() {
        _error = true;
      });
    }
  }

  @override
  void initState() {
    initializeFlutterFire();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        home: Scaffold(
      appBar: AppBar(
        title: Text("Mpesa Demo"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              'Your account balance:',
            ),
            StreamBuilder(
              stream: getAccountBalance(),
              builder: (BuildContext context,
                  AsyncSnapshot<DocumentSnapshot> snapshot) {
                if (snapshot == null ||
                    snapshot.connectionState == ConnectionState.waiting) {
                  return CircularProgressIndicator(
                    strokeWidth: 1.0,
                  );
                } else if (snapshot.connectionState == ConnectionState.active) {
                  if (snapshot.data.data() != null) {
                    Map<String, dynamic> documentFields = snapshot.data.data();

                    return Text(
                      documentFields.containsKey("wallet")
                          ? documentFields["wallet"].toString()
                          : "0",
                      style: TextStyle(
                          fontSize: 20.0, fontWeight: FontWeight.w800),
                    );
                  } else {
                    return Text(
                      '0!',
                    );
                  }
                } else {
                  return Text("!");
                }
              },
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          // Show error message if initialization failed
          if (_error) {
            print("Error initializing Fb");
            return;
          }

          // Show a loader until FlutterFire is initialized
          if (!_initialized) {
            print("Fb Not initialized");
            return;
          }

          startTransaction(amount: 2.0, phone: "254 your phone here");
        },
        tooltip: 'Increment',
        label: Text("Top Up"),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    ));
  }
}

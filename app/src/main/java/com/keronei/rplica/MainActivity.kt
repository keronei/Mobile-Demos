package com.keronei.rplica

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

enum class Operator {
    ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, EQUALS, WAITING
}

enum class NextInput {
    OPERATOR, DIGIT
}
/*
Simple implementation of calculator operators - display the result while the user intends to
perform another operation
 */

class MainActivity : AppCompatActivity() {

    //Flag to determine if we're starting fresh calculation
    private var isFirstInput = true

    //As the user provides next input, keep their operator here
    private var receivedNextOp: Operator = Operator.WAITING

    //result as-at-now
    var currentDisplay: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        result.text = currentDisplay.toString()
        /* Handle inputs

        * A number will be DIGIT
        * Anything else will be Operator
        * Case a number is provided, operator is null and vise versa
         */
        zero_button.setOnClickListener {
            handleNewInputAndDisplay(NextInput.DIGIT, zero_button.text.toString().toInt(), null)

        }

        one_button.setOnClickListener {
            handleNewInputAndDisplay(NextInput.DIGIT, one_button.text.toString().toInt(), null)
        }

        two_button.setOnClickListener {
            handleNewInputAndDisplay(NextInput.DIGIT, two_button.text.toString().toInt(), null)
        }

        three_button.setOnClickListener {
            handleNewInputAndDisplay(NextInput.DIGIT, three_button.text.toString().toInt(), null)
        }

        multiply_button.setOnClickListener {
            handleNewInputAndDisplay(NextInput.OPERATOR, null, Operator.MULTIPLICATION)
        }

        add_button.setOnClickListener {
            handleNewInputAndDisplay(NextInput.OPERATOR, null, Operator.ADDITION)
        }

        minu_button.setOnClickListener {
            handleNewInputAndDisplay(NextInput.OPERATOR, null, Operator.SUBTRACTION)
        }
        equal_button.setOnClickListener {
            handleNewInputAndDisplay(NextInput.OPERATOR, null, Operator.EQUALS)

        }



        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        clear_button.setOnClickListener {
            currentDisplay = 0
            isFirstInput = true
            result.text = "0"
            receivedNextOp = Operator.WAITING
        }
    }

    private fun handleNewInputAndDisplay(input: NextInput, value: Int? = 0, operator: Operator?) {

        when (input) {
            NextInput.DIGIT -> {

                if (value != null) {

                    when {
                        isFirstInput -> {
                            // Is first one, switch firstInput flag to false
                            currentDisplay = value
                            isFirstInput = false
                            result.text = currentDisplay.toString()

                        }
                        receivedNextOp == Operator.WAITING -> {
                            // Under no clear circumstances would this execute, but its better to be clear
                            isFirstInput = false
                            currentDisplay = value
                            result.text = currentDisplay.toString()

                        }
                        else -> {
                            when (receivedNextOp) {
                                /* When inputs are sent and there's an operand in waiting,
                                perform an operation but don't update the display yet
                                * */
                                Operator.ADDITION -> {
                                    currentDisplay += value
                                    result.text = value.toString()
                                    receivedNextOp = Operator.WAITING
                                }

                                Operator.DIVISION -> {
                                    currentDisplay /= value
                                    result.text = value.toString()
                                    receivedNextOp = Operator.WAITING

                                }

                                Operator.MULTIPLICATION -> {
                                    currentDisplay *= value
                                    result.text = value.toString()
                                    receivedNextOp = Operator.WAITING

                                }

                                Operator.SUBTRACTION -> {
                                    currentDisplay -= value
                                    result.text = value.toString()
                                    receivedNextOp = Operator.WAITING

                                }
                                Operator.EQUALS -> {
                                    /* possibly wont reach here, i.e there's no time that
                                    receivedNextOp would store an = ,
                                    but for case completeness, keep it here
                                    */
                                }


                                Operator.WAITING -> {
                                    //Do nothing, but if a new number comes in, reset
                                    isFirstInput = true
                                    currentDisplay = value
                                    result.text = currentDisplay.toString()
                                }
                            }


                        }
                    }
                }
            }

            NextInput.OPERATOR -> {
                if (operator != null && operator != Operator.EQUALS) {
                    //save the operator
                    receivedNextOp = operator
                    result.text = currentDisplay.toString()
                } else if (operator == Operator.EQUALS)
                    /*the user has completed first calculation, display the current answer
                    reset the operator to avoid leaks
                    * */
                    receivedNextOp = Operator.WAITING
                    result.text = currentDisplay.toString()
            }
        }



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

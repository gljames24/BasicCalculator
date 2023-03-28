package com.example.basiccalculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.basiccalculator.databinding.ActivityMainBinding

enum class SymbolType {
    DIGIT, ARITHMETIC_SYMBOL, DECIMAL_POINT, NEGATIVE_SYMBOL, EXPONENT
}

class MainActivity : AppCompatActivity() {

    private var hasDecimal = false
    private var hasOperator = false
    private var hasExponent: Boolean = false

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun clearEquation(view: View){
        binding.equationTextView.text = "_"
        hasDecimal = false
        hasOperator = false
        hasExponent = false
    }

    private fun checkSymbol(char: Char): SymbolType {
        return when (char) {
            in '0'..'9' -> SymbolType.DIGIT
            '×', '÷', '+', '–' -> SymbolType.ARITHMETIC_SYMBOL
            '.' -> SymbolType.DECIMAL_POINT
            '-','±' -> SymbolType.NEGATIVE_SYMBOL
            else -> SymbolType.EXPONENT
        }
    }

    private fun containsDecimal(charSequence: CharSequence): Boolean {
        val text = charSequence.toString()
        val lastDigits = text.replaceFirst(Regex(".*[^\\d.]+([\\d.]+)\\D*$"), "$1")
        if (lastDigits.isEmpty() || lastDigits.last() == '.') {
            return false
        }
        return lastDigits.contains('.')
    }


    fun inputAction(view: View) {
        if(view is Button){
            val symbolType = checkSymbol(view.text.get(0))
            when (symbolType) {
                SymbolType.DIGIT -> {
                    if(binding.equationTextView.text.contains("_")){
                        binding.equationTextView.text=""
                    }
                    val digit: CharSequence
                    if(hasExponent && !hasOperator){
                        digit = when(view.text){
                            "0" -> "⁰"
                            "1" -> "¹"
                            "2" -> "²"
                            "3" -> "³"
                            "4" -> "⁴"
                            "5" -> "⁵"
                            "6" -> "⁶"
                            "7" -> "⁷"
                            "8" -> "⁸"
                            "9" -> "⁹"
                            else -> ""
                        }
                        binding.equationTextView.append(digit)
                        hasOperator = true
                    }
                    else if(!hasExponent){
                        binding.equationTextView.append(view.text)
                        hasOperator = false
                    }
                }
                SymbolType.ARITHMETIC_SYMBOL -> {
                    if((!hasOperator || hasExponent) && !(binding.equationTextView.text=="_")){
                        binding.equationTextView.append(view.text)
                        hasOperator = true
                        hasDecimal = false
                        hasExponent = false
                    }
                }
                SymbolType.DECIMAL_POINT -> {
                    if(!hasDecimal && !hasExponent){
                        if(binding.equationTextView.text.contains("_")){
                            binding.equationTextView.text=""
                        }
                        binding.equationTextView.append(".")
                        hasDecimal = true
                    }
                }
                SymbolType.NEGATIVE_SYMBOL -> {
                    if(binding.equationTextView.text.last().equals("-")){
                        binding.equationTextView.text = binding.equationTextView.text.subSequence(0, binding.equationTextView.length() - 1)
                        if(binding.equationTextView.text==""){
                            binding.equationTextView.text="_"
                        }
                    }
                    else if(hasOperator && !hasExponent){
                        binding.equationTextView.append("-")
                    }
                    else if(binding.equationTextView.text=="_"){
                        binding.equationTextView.text="-"
                    }
                }
                SymbolType.EXPONENT -> {
                    if(!hasOperator && !hasExponent){
                        hasExponent = true
                    }
                }
            }
        }
    }

    fun invertSignAction(view: View) {
        if(binding.equationTextView.text.last().equals("-")){
            binding.equationTextView.text = binding.equationTextView.text.subSequence(0, binding.equationTextView.length() - 1)
            if(binding.equationTextView.text==""){
                binding.equationTextView.text="_"
            }
        }
        else if(hasOperator && !hasExponent){
            binding.equationTextView.append("-")
        }
        else if(binding.equationTextView.text=="_"){
            binding.equationTextView.text="-"
        }
    }

    fun backspaceAction(view: View) {
        val eqLength = binding.equationTextView.length()
        if(eqLength > 1){
            binding.equationTextView.text = binding.equationTextView.text.subSequence(0, eqLength - 1)
            when (checkSymbol(binding.equationTextView.text.last())) {
                SymbolType.DIGIT -> {
                    hasDecimal = containsDecimal(binding.equationTextView.text)
                    hasOperator = false
                    hasExponent = false
                }
                SymbolType.ARITHMETIC_SYMBOL -> {
                    hasDecimal = false
                    hasOperator = true
                    hasExponent = false
                }
                SymbolType.DECIMAL_POINT -> {
                    hasDecimal = true
                    hasOperator = true
                    hasExponent = false
                }
                SymbolType.NEGATIVE_SYMBOL -> {
                    hasDecimal = false
                    hasOperator = true
                    hasExponent = false
                }
                SymbolType.EXPONENT -> {
                    hasDecimal = false
                    hasOperator = true
                    hasExponent = true
                }
            }
        }
        else if(eqLength == 1){
            clearEquation(view)
        }
    }

    fun clearAction(view: View) {
        clearEquation(view)
        binding.resultsTextView.text = ""
    }

    fun returnAction(view: View) {
        binding.resultsTextView.text = ""
        binding.resultsTextView.text = calculateResult()
        clearEquation(view)
    }

    private fun calculateResult(): String {
        return parsedEquation(binding.equationTextView.text).toString()
    }

//    private fun exponentiate(parsedEquation: MutableList<Any>): Any {
//
//    }


    private fun parsedEquation(input : CharSequence): MutableList<String>{
        val output = mutableListOf<String>()
        var lastEndIndex = 0
        for (i in input.indices) {
            val c = input[i]
            if (c in "-.0123456789") {
                if (i == input.lastIndex || input[i+1] !in ".0123456789") {
                    val subsequence = input.subSequence(lastEndIndex, i+1).toString()
                    output.add(subsequence)
                    lastEndIndex = i + 1
                }
            } else if (c in "+×÷—") {
                output.add(c.toString())
                lastEndIndex = i + 1
            } else if (c in "⁰¹²³⁴⁵⁶⁷⁸⁹") {
                output.add(c.toString())
                lastEndIndex = i + 1
            }
        }
        return output
    }

    private fun superscriptToDigit(superscript: Char): Char {
        return when (superscript) {
            '⁰' -> '0'
            '¹' -> '1'
            '²' -> '2'
            '³' -> '3'
            '⁴' -> '4'
            '⁵' -> '5'
            '⁶' -> '6'
            '⁷' -> '7'
            '⁸' -> '8'
            '⁹' -> '9'
            else -> throw IllegalArgumentException("Invalid superscript character: $superscript")
        }
    }

}
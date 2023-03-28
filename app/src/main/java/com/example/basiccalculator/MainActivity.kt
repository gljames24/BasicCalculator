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
                    if((!hasOperator || hasExponent) && !(binding.equationTextView.text.contains("_"))){
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
                    else if(binding.equationTextView.text.contains("_")){
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
            if(binding.equationTextView.text.equals("")){
                binding.equationTextView.text="_"
            }
        }
        else if(hasOperator && !hasExponent){
            binding.equationTextView.append("-")
        }
        else if(binding.equationTextView.text.equals("_")){
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
        return binding.equationTextView.text.parseEquation().parseExponent().parseMultDiv().parseAddSub()[0]
    }

    private fun CharSequence.parseEquation(): MutableList<String>{
        val output = mutableListOf<String>()
        var lastEndIndex = 0
        for (i in this.indices) {
            val c = this[i]
            if (c in "-.0123456789") {
                if (i == this.lastIndex || this[i+1] !in ".0123456789") {
                    val subsequence = this.subSequence(lastEndIndex, i+1).toString()
                    output.add(subsequence)
                    lastEndIndex = i + 1
                }
            } else if (c in "+×÷–") {
                output.add(c.toString())
                lastEndIndex = i + 1
            } else if (c in "⁰¹²³⁴⁵⁶⁷⁸⁹") {
                output.add(c.toString())
                lastEndIndex = i + 1
            }
        }
        return output
    }

    fun MutableList<String>.parseExponent(): MutableList<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < this.size) {
            if (i + 1 < this.size && this[i + 1].isSuperscriptDigit()) {
                val numberString = this[i]
                val superscriptChar = this[i + 1][0]
                val base = numberString.toDoubleOrNull() ?: 0.0
                val exponent = superscriptToDigit(superscriptChar).toString().toDoubleOrNull() ?: 0.0
                val powResult = Math.pow(base, exponent).toString()
                result.add(powResult)
                i += 2
            } else {
                result.add(this[i])
                i++
            }
        }
        return result
    }

    fun MutableList<String>.parseMultDiv(): MutableList<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < this.size) {
            val token = this[i]
            if (token == "×" || token == "÷") {
                val left = result.removeLast().toDouble()
                val right = this[i + 1].toDouble()
                val value = when (token) {
                    "×" -> left * right
                    "÷" -> left / right
                    else -> throw IllegalArgumentException("Unknown operator: $token")
                }
                result.add(value.toString())
                i += 2
            } else {
                result.add(token)
                i++
            }
        }
        return result
    }

    fun MutableList<String>.parseAddSub(): MutableList<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i < this.size) {
            val token = this[i]
            if (token == "+" || token == "–") {
                val left = result.removeLast().toDouble()
                val right = this[i + 1].toDouble()
                val value = when (token) {
                    "+" -> left + right
                    "–" -> left - right
                    else -> throw IllegalArgumentException("Unknown operator: $token")
                }
                result.add(value.toString())
                i += 2
            } else {
                result.add(token)
                i++
            }
        }
        return result
    }

    private fun superscriptToDigit(superscript: Char): Double {
        return when (superscript) {
            '⁰' -> 0.0
            '¹' -> 1.0
            '²' -> 2.0
            '³' -> 3.0
            '⁴' -> 4.0
            '⁵' -> 5.0
            '⁶' -> 6.0
            '⁷' -> 7.0
            '⁸' -> 8.0
            '⁹' -> 9.0
            else -> -1.0
        }
    }

    fun String.isSuperscriptDigit(): Boolean {
        return superscriptToDigit(this[0])!=-1.0
    }

}
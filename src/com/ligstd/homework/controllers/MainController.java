package com.ligstd.homework.controllers;

import com.ligstd.homework.calculators.DerivationCalculator;
import com.ligstd.homework.calculators.SimplifyCalculator;
import com.ligstd.homework.enums.CommandEnum;
import com.ligstd.homework.models.Command;
import com.ligstd.homework.models.SubItem;
import com.ligstd.homework.parsers.CommandParser;
import com.ligstd.homework.parsers.ExpressionParser;
import com.ligstd.homework.utils.Utils;

import java.io.*;
import java.util.List;
import java.util.Map;
/**
 *
 * @author me
 *
 */
public class MainController {
    /**
     *
     */
    private static final CommandParser
        commandParser = new CommandParser();
    /**
     *
     */
    private static final ExpressionParser
        expressionParser = new ExpressionParser();
    /**
     *
     */
    private static final SimplifyCalculator
        simplifyCalculator = new SimplifyCalculator();
    /**
     *
     */
    private static final DerivationCalculator
        derivationCalculator = new DerivationCalculator();
    /**
     *
     */
    private BufferedReader inputReader;
    /**
     *
     */
    private PrintStream outputStream;
    /**
     *
     */
    private List<SubItem> currentExpression;
    /**
     *
     */
    private Command currentCommand;
    /**
     *
     * @return ?
     */
    public static CommandParser getCommandParser() {
        return commandParser;
    }
    /**
     *
     * @return ?
     */
    public static ExpressionParser getExpressionParser() {
        return expressionParser;
    }
    /**
     *
     * @return ?
     */
    public static SimplifyCalculator getSimplifyCalculator() {
        return simplifyCalculator;
    }
    /**
     *
     * @return ?
     */
    public static DerivationCalculator getDerivationCalculator() {
        return derivationCalculator;
    }
    /**
     *
     * @return ?
     */
    public final BufferedReader getInputReader() {
        return inputReader;
    }
    /**
     *
     * @param inputReader ?
     */
    public final void setInputReader(BufferedReader inputReader) {
        this.inputReader = inputReader;
    }
    /**
     *
     * @return ?
     */
    public PrintStream getOutputStream() {
        return outputStream;
    }
    /**
     *
     * @param outputStream ?
     */
    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }
    /**
     *
     * @return ?
     */
    public List<SubItem> getCurrentExpression() {
        return currentExpression;
    }
    /**
     *
     * @param currentExpression ?
     */
    public void setCurrentExpression(List<SubItem> currentExpression) {
        this.currentExpression = currentExpression;
    }
    /**
     *
     * @return ?
     */
    public Command getCurrentCommand() {
        return currentCommand;
    }
    /**
     *
     * @param currentCommand ?
     */
    public void setCurrentCommand(Command currentCommand) {
        this.currentCommand = currentCommand;
    }
    /**
     *
     * @param inputStream ?
     * @param outputStream ?
     */
    public MainController(InputStream inputStream, PrintStream outputStream) {
        setInputReader(new BufferedReader(new InputStreamReader(inputStream)));
        setOutputStream(outputStream);
    }
    /**
     *
     * @throws IOException ?
     */
    public void AcquireInput() throws IOException {
        getOutputStream().print('>');
        String currentInput = getInputReader().readLine();
        if (currentInput != null) {
            ParseInput(currentInput);
        }
    }
    /**
     *
     * @param input ?
     */
    private void ParseInput(String input) {
        if (input.startsWith("!")) {
            getCommandParser().setInput(input);
            getCommandParser().Parse();
            setCurrentCommand(getCommandParser().getResult());
            Calculate();
        } else {
            getExpressionParser().setInput(input);
            getExpressionParser().Parse();
            if (null != getCurrentExpression()) getCurrentExpression().clear();
            setCurrentExpression(getExpressionParser().getResult());
            Feedback(getCurrentExpression());
        }
    }
    /**
     *
     */
    private void Calculate() {
        if (getCurrentExpression() == null) throw new ArithmeticException("Error, Expression Undefined.");
        if (getCurrentCommand().getType() == CommandEnum.Simplify) {
            getSimplifyCalculator().setCommand(getCurrentCommand());
            getSimplifyCalculator().setExpression(getCurrentExpression());
            getSimplifyCalculator().Calculate();
            Feedback(getSimplifyCalculator().getNewExpression());
            getSimplifyCalculator().getNewExpression().clear();
        } else {
            getDerivationCalculator().setCommand(getCurrentCommand());
            getDerivationCalculator().setExpression(getCurrentExpression());
            getDerivationCalculator().Calculate();
            Feedback(getDerivationCalculator().getNewExpression());
            getDerivationCalculator().getNewExpression().clear();
        }
        System.gc();
    }
    /**
     *
     * @param resultExpression ?
     */
    private void Feedback(List<SubItem> resultExpression) {
        Integer expressionSize = resultExpression.size();
        String[] subItemStrings = new String[expressionSize];
        for (Integer subItemIndex = 0; subItemIndex < expressionSize; subItemIndex++) {
            SubItem currentSubItem = resultExpression.get(subItemIndex);
            Map<String, Double> variables = currentSubItem.getVariables();
            Double coefficient = currentSubItem.getCoefficient();
            if (null == variables) {
                subItemStrings[subItemIndex] = Utils.RemoveZeros(String.format("+%f", currentSubItem.getCoefficient()));
            } else {
                if (coefficient == 1) {
                    subItemStrings[subItemIndex] = "+";
                } else if (coefficient == -1) {
                    subItemStrings[subItemIndex] = "-";
                }
                else{
                    subItemStrings[subItemIndex] = Utils.RemoveZeros(String.format("+%f", currentSubItem.getCoefficient())) + "*";
                }
                Integer variablesSize = variables.size();
                String[] variableStrings = new String[variablesSize];
                Integer currentIndex = 0;
                for (String variableName : variables.keySet()) {
                    Double power = variables.get(variableName);
                    if (power == 1) {
                        variableStrings[currentIndex] = variableName;
                    } else {
                        String powerString = Utils.RemoveZeros(power.toString());
                        variableStrings[currentIndex] = String.format("%s^%s", variableName, powerString);
                    }
                    currentIndex++;
                }
                subItemStrings[subItemIndex] += String.join("*", (CharSequence[]) variableStrings);
            }
        }
        String resultString = Utils.PostProcessMinus(String.join("", (CharSequence[]) subItemStrings));
        if (resultString.startsWith("+*")) resultString = resultString.substring(2);
        else if (resultString.startsWith("+")) resultString = resultString.substring(1);
        outputStream.println(resultString);
    }
}

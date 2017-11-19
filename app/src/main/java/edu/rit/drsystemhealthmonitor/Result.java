package edu.rit.drsystemhealthmonitor;

/**
 * Created by Chris on 11/3/2017.
 */

public class Result {
    String input;
    String output;
    String action;

    public Result(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String toString() {
        return "action: " + action + '\n' + "input: " + input + '\n' + "output: " + output + '\n';
    }
}

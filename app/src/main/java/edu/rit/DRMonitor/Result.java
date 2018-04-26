package edu.rit.DRMonitor;

/**
 * Object representation of a response sent by Pi's server (not used currently)
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

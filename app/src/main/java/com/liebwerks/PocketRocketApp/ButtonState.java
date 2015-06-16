package com.liebwerks.PocketRocketApp;

/**
 * Created by klieberman on 6/2/15.
 */
public class ButtonState {
    long time;
    boolean redState;
    boolean greenState;
    boolean blueState;
    boolean orangeState;
    boolean yellowState;
    boolean purpleState;


    ButtonState(long time, boolean redState, boolean greenState, boolean blueState, boolean orangeState, boolean yellowState, boolean purpleState) {

        this.time = time;
        this.redState = redState;
        this.greenState = greenState;
        this.blueState = blueState;
        this.orangeState = orangeState;
        this.yellowState = yellowState;
        this.purpleState = purpleState;
    }

    /* currently used to output to file */
    public String toString() {
        return(time + "," + redState + "," + greenState + "," + blueState + "," + orangeState + "," + yellowState + "," + purpleState);
    }
}

package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

//Ofek
public class TickBroadcast implements Broadcast {

    private int currentTick;

    public TickBroadcast(int tick) {this.currentTick = tick;}

    public int getTick(){
        return this.currentTick;
    }

    public void increasTick() {
        currentTick = currentTick + 1;
    }
}

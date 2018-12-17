package bgu.spl.mics.application.messages;
//Ofek
public class TickBroadcast {

    private int currentTick;

    public TickBroadcast(int tick) {this.currentTick = tick;}

    public int getTick(){
        return this.currentTick;
    }

    public void increasTick() {
        currentTick = currentTick + 1;
    }
}

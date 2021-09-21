package com.example.myavaudiorecorder.Model;


public class Info{
    public String speech = null;
    public int channel = 1;
    public int rate = 16000;
    public long len = 0;
    public String cuid = "A-5183-9296-87E0EF0EF15C";
    public String format = "m4a";
    public String token = "";

    public Info(String speech, long len, String token){
        this.speech = speech;
        this.len = len;
        this.token = token;
    }
    public String toString(){
        String info = "";
        info += "speech: " +this.speech + "\nchannel: " + this.channel
                + "\nrate: " + this.rate + "\n len: " + this.len + "\ncuid: " + this.cuid
                + "\nformat: " + this.format + "\ntoken: " + this.token;
        return info;
    }

}

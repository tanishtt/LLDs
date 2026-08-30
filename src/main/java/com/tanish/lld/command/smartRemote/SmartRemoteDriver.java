package com.tanish.lld.command.smartRemote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * ============================================================
 *                         COMMAND
 * ============================================================
 *
 * Command represents an action that can be executed/undone.
 */
interface Command{
    void execute();
    void undo();
}

/*
 * ============================================================
 *                       RECEIVERS
 * ============================================================
 *
 * Receivers contain the actual device/business logic.
 */
//LIGHT
class Light{
    private boolean on;
    private int brightness=50;
    public void turnOn(){
        on=true;
        System.out.println("Light turned ON.");
    }
    public void turnOff(){
        on=false;
        System.out.println("Light turned OFF.");
    }
    public void setBrightness(int brightness){
        if (brightness < 0 || brightness > 100) {
            throw new IllegalArgumentException("Brightness must be between 0 and 100");
        }
        this.brightness=brightness;
        System.out.println("Light brightness set to " + brightness);
    }

    public boolean isOn() {
        return on;
    }

    public int getBrightness() {
        return brightness;
    }
}

//FAN
class Fan{
    private boolean on;
    private int speed;
    public void turnOn(){
        on=true;
        System.out.println("Fan turned ON.");
    }
    public void turnOff(){
        on=false;
        System.out.println("Fan turned OFF.");
    }
    public void setSpeed(int speed){
        if (speed < 0 || speed > 5) {
            throw new IllegalArgumentException("Fan speed must be between 0 and 5");
        }
        this.speed=speed;
        System.out.println("Fan speed set to "+speed);
    }

    public boolean isOn() {
        return on;
    }

    public int getSpeed() {
        return speed;
    }
}
//TV
class TV{
    private boolean on;
    private int volume;
    private int channel;

    public void turnOn() {
        on = true;
        System.out.println("TV turned ON");
    }

    public void turnOff() {
        on = false;
        System.out.println("TV turned OFF");
    }

    public void setVolume(int volume) {

        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException(
                    "Volume must be between 0 and 100"
            );
        }

        this.volume = volume;

        System.out.println(
                "TV volume set to " + volume
        );
    }

    public void setChannel(int channel) {

        if (channel <= 0) {
            throw new IllegalArgumentException(
                    "Channel must be positive"
            );
        }

        this.channel = channel;

        System.out.println(
                "TV channel changed to " + channel
        );
    }

    public boolean isOn() {
        return on;
    }

    public int getVolume() {
        return volume;
    }

    public int getChannel() {
        return channel;
    }
}

/*
 * ============================================================
 *                 CONCRETE COMMANDS
 * ============================================================
 */
class LightOnCommand implements Command{
private final Light light;

    LightOnCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.turnOn();
    }

    @Override
    public void undo() {
        light.turnOff();
    }
}
class LightOffCommand implements Command{
    private final Light light;

    LightOffCommand(Light light) {
        this.light = light;
    }

    @Override
    public void execute() {
        light.turnOff();
    }

    @Override
    public void undo() {
light.turnOn();
    }
}
class FanOnCommand implements Command{
    private final Fan fan;

    FanOnCommand(Fan fan) {
        this.fan = fan;
    }

    @Override
    public void execute() {
        fan.turnOn();
    }

    @Override
    public void undo() {
        fan.turnOff();
    }
}
class FanOffCommand implements Command{
private final Fan fan;

    FanOffCommand(Fan fan) {
        this.fan = fan;
    }

    @Override
    public void execute() {
        fan.turnOff();
    }

    @Override
    public void undo() {
fan.turnOn();
    }
}
class TVOnCommand implements Command {

    private final TV tv;

    public TVOnCommand(TV tv) {
        this.tv = tv;
    }

    @Override
    public void execute() {
        tv.turnOn();
    }

    @Override
    public void undo() {
        tv.turnOff();
    }
}
class TVOffCommand implements Command {

    private final TV tv;

    public TVOffCommand(TV tv) {
        this.tv = tv;
    }

    @Override
    public void execute() {
        tv.turnOff();
    }

    @Override
    public void undo() {
        tv.turnOn();
    }
}
/*
 * ============================================================
 *                PARAMETERIZED COMMAND
 * ============================================================
 *
 * This demonstrates that Command can encapsulate not only
 * the receiver but also the data required for the operation.
 */
//TV VOLUME COMMAND
class SetTVVolumeCommand implements Command{
    private final TV tv;
    private final int newVolume;

    private int previousVolume;

    SetTVVolumeCommand(TV tv, int newVolume) {
        this.tv = tv;
        this.newVolume = newVolume;
    }

    @Override
    public void execute() {
        previousVolume=tv.getVolume();
        tv.setVolume(newVolume);
    }

    @Override
    public void undo() {
        tv.setVolume(previousVolume);
    }
}
//FAN SPEED COMMAND
class SetFanSpeedCommand implements Command{
    private final Fan fan;
    private final int newSpeed;

    private int previousSpeed;

    SetFanSpeedCommand(Fan fan, int newSpeed) {
        this.fan = fan;
        this.newSpeed = newSpeed;
    }

    @Override
    public void execute() {
        previousSpeed=fan.getSpeed();
        fan.setSpeed(newSpeed);
    }

    @Override
    public void undo() {
        fan.setSpeed(previousSpeed);
    }
}
/*
 * ============================================================
 *                    MACRO COMMAND
 * ============================================================
 *
 * A MacroCommand combines multiple Commands and treats them
 * as a single command.
 *
 * Example:
 *
 * Movie Mode:
 *
 *     Light OFF
 *     TV ON
 *     Fan ON
 *     Fan speed = 2
 */
class MacroCommand implements Command{
    private final List<Command> commands;

    MacroCommand(List<Command> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new IllegalArgumentException("Commands cannot be empty");
        }
        this.commands = commands;
    }

    @Override
    public void execute() {
        for (Command command:commands){
            command.execute();
        }
    }

    @Override
    public void undo() {
        for (int i=commands.size()-1;i>=0;i--){
            commands.get(i).undo();
        }

    }
}
/*
 * ============================================================
 *                    REMOTE CONTROL
 * ============================================================
 *
 * INVOKER
 *
 * RemoteControl does NOT know about Light/Fan/TV.
 *
 * It only knows Command.
 */
class RemoteControl{
    private final Map<Integer, Command> buttons=new HashMap<>();
    private Command lastCommand;
    public void setCommand(int button, Command command){
        if (button < 1) {
            throw new IllegalArgumentException("Button number must be positive");
        }
        buttons.put(button, command);
    }
    public void pressButton(int button){
        Command command=buttons.get(button);
        if (command == null) {
            System.out.println(
                    "No command configured for button " + button
            );
            return;
        }
        command.execute();
        lastCommand=command;
    }
    public void pressUndo(){
        if (lastCommand == null) {
            System.out.println("Nothing to undo");
            return;
        }
        lastCommand.undo();
        lastCommand=null;
    }
}
public class SmartRemoteDriver {
    public static void main(String[] args) {
        //create receivers
        Light livingRoomLight=new Light();
        Fan livingRoomFan=new Fan();
        TV livingRoomTV=new TV();

        //create commands
        Command lightOn =
                new LightOnCommand(livingRoomLight);

        Command lightOff =
                new LightOffCommand(livingRoomLight);

        Command fanOn =
                new FanOnCommand(livingRoomFan);

        Command fanOff =
                new FanOffCommand(livingRoomFan);

        Command tvOn =
                new TVOnCommand(livingRoomTV);

        Command tvOff =
                new TVOffCommand(livingRoomTV);

        Command fanSpeed2 =
                new SetFanSpeedCommand(
                        livingRoomFan,
                        2
                );

        Command tvVolume30 =
                new SetTVVolumeCommand(
                        livingRoomTV,
                        30
                );
        Command movieMode = new MacroCommand(
                List.of(
                        lightOff,
                        tvOn,
                        fanOn,
                        fanSpeed2,
                        tvVolume30
                )
        );

        //configurations, set command
        RemoteControl remote=new RemoteControl();
        remote.setCommand(1, lightOn);
        remote.setCommand(2,lightOff);

        remote.setCommand(3,fanOn);
        remote.setCommand(4,fanOff);

        remote.setCommand(5,tvOn);
        remote.setCommand(6,tvOff);

        remote.setCommand(7,fanSpeed2);
        remote.setCommand(8,tvVolume30);

        remote.setCommand(9,movieMode);

        System.out.println("-----individual commands-----");
        remote.pressButton(1);
        // Light turned ON

        remote.pressButton(3);
        // Fan turned ON

        remote.pressButton(7);
        // Fan speed set to 2

        remote.pressButton(5);
        // TV turned ON

        remote.pressButton(8);
        // TV volume set to 30
        System.out.println("\n----- Undo -----");
        remote.pressUndo();

        System.out.println("\n----- Movie Mode -----");
        remote.pressButton(9);
        System.out.println("\n----- Undo Movie Mode -----");
        remote.pressUndo();

    }
}

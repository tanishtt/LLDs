package com.tanish.lld.decorator.TextFormattingDriver;

interface Text{
    String render();
    String getPlainText();
}

class PlainText implements Text{

    private final String content;

    PlainText(String content) {
        this.content = content;
    }

    @Override
    public String render() {
        return content;
    }

    @Override
    public String getPlainText() {
        return content;
    }
}
class HTMLText implements Text{
private final String content;

    HTMLText(String content) {
        this.content = content;
    }

    @Override
    public String render() {
        return content;
    }

    @Override
    public String getPlainText() {
        return content;
    }
}
class MarkDownText implements Text{
    private final String content;

    MarkDownText(String content) {
        this.content = content;
    }

    @Override
    public String render() {
        return content;
    }

    @Override
    public String getPlainText() {
        return content;
    }
}


//decorator
abstract class TextDecorator implements Text{
    protected final Text wrappee;

    TextDecorator(Text wrappee) {
        this.wrappee = wrappee;
    }

    @Override
    public String render(){
        return wrappee.render();
    }
    @Override
    public String getPlainText(){
        return wrappee.getPlainText();
    }
}
class BoldText extends TextDecorator{
    BoldText(Text wrappee) {
        super(wrappee);
    }

    @Override
    public String render(){
        return "<b>" + wrappee.render() + "</b>";
    }
}
class ItalicText extends TextDecorator{
    ItalicText(Text wrappee) {
        super(wrappee);
    }
    @Override
    public String render(){
        return "<i>" + wrappee.render() + "</i>";
    }
}
class ColorText extends TextDecorator{
    private final String color;
    ColorText(Text wrappee, String color) {
        super(wrappee);
        this.color = color;
    }
    @Override
    public String render(){
        return "<span style=\"color:" + color + "\">" + wrappee.render() + "</span>";
    }
}
//etc...


class TextFormatBuilder{
    private Text plainText;
    public TextFormatBuilder(String rawContent){
        plainText=new PlainText(rawContent);
    }
    public TextFormatBuilder bold(){
        plainText=new BoldText(plainText);
        return this;
    }
    public TextFormatBuilder italic(){
        plainText=new ItalicText(plainText);
        return this;
    }
    public TextFormatBuilder color(String hex){
        plainText=new ColorText(plainText, hex);
        return this;
    }
    public Text build(){
        return plainText;
    }
}
public class TextFormattingDriver {
    public static void main(String[] args) {
        Text text= new TextFormatBuilder("hi there, how are you...")
                .bold()
                .italic()
                .color("#34ff5r")
                .build();

        System.out.println("Rendered: "+text.render());

    }
}

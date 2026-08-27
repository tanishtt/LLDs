# Text Formatter — Simple LLD Interview Question

## Problem Statement
Design a simple Text Formatter using the Decorator pattern. The formatter should start from a base text object and allow adding formatting decorators (e.g., bold, italic, color) that wrap the text and modify its rendered output.

Keep the design small and focused: no networking, no plugin system, no persistence — just in-memory objects and composition.

## Requirements
- Implement a Text interface with methods: render() and getPlainText().
- Provide base implementations (PlainText, HTMLText, MarkDownText).
- Implement decorators: BoldText, ItalicText, ColorText that wrap a Text and augment render().
- Provide a fluent builder (TextFormatBuilder) to compose decorations and build a final Text object.
- Demonstrate usage in a main/driver that prints the rendered result.

## Example Usage
Text text = new TextFormatBuilder("hello world")
    .bold()
    .italic()
    .color("#ff0000")
    .build();
System.out.println(text.render());

## Expected Behavior
- Decorations are applied in wrapper order (outermost decorator appears last in builder calls).
- getPlainText() always returns original unformatted content.
- render() returns the content with HTML-style tags for demo (e.g., <b>, <i>, <span style="color:...">).

## Test Ideas
- PlainText only returns same content for render() and getPlainText().
- Applying BoldText adds <b> wrapper around render().
- Chaining multiple decorators nests tags correctly.
- ColorText uses the provided color value.

## Extension (optional)
- Add underline, strikethrough, or font-size decorators.
- Support Markdown or HTML-specific decorators that produce different tag styles.


This simplified question matches the existing TextFormattingDriver implementation in this package.
#About Barbarossa#
Barbarossa is a lightweight technology for developing Webpages that supports dynamic content
without the need for servlets. It allows developers to use a simple tagging system to integrate
Java code into HTML pages in a type safe manner. Barbarossa also supports layouts.


# Background #
- [Basic Syntax](#basic-syntax)
  * [String Content](#string-content)
  * [Statements](#statements)
  * [Expressions](#expressions)
  * [Directives](#directives)
  * [Comments](#comments)
- [Directive Keywords](#directive-keywords)
  * [Import](#import)
  * [extends](#extends)
  * [Include](#include)
  * [Params](#params)
  * [Section](#section)

# Basic Syntax #
## String Content ##
String Content is the "default" token. Anything in a string content will be generated
as-is in the resulting file.

There is no special syntax to denote a String Content. Rather, anything that is not
wrapped within an expression, statement, directive, or comment will be processed
as String Content.
## Statements ##
Statements are similar to JSP scriptlets: they can contain any number of Java
language statements, including variable or method declarations. The validity of
statements are not evaluated by the Barbarossa compiler; rather, they are evaluated
in the generated Java files at compile-time/runtime.

The syntax of a statement is as follows:
```jsp
<% code fragment %>
```

Note that any text within a statement *must be valid code in Java*. For example,
the statement
```jsp
<% System.out.println("Hello") %>
```
will result in the following Java code being generated:
```java
System.out.println("Hello")
```
which will result in a compiler error (there is no semicolon to end the line).

## Expressions ##
Expressions are similar to JSP expressions. As explained in [this JSP guide:](https://www.tutorialspoint.com/jsp/jsp_syntax.htm)
>A JSP expression element contains a scripting language expression that is evaluated, converted to a String, and inserted where the expression appears in the JSP file.

>Because the value of an expression is converted to a String, you can use an expression within a line of text, whether or not it is tagged with HTML, in a JSP file.

>The expression element can contain any expression that is valid according to the Java Language Specification but you cannot use a semicolon to end an expression.

The syntax of an expression is as follows:
```jsp
<%= expression %>
```

Additionally, the following syntax is also valid:
```jsp
${ expression }
```

For example, you can do the following with expressions:
```jsp
<html>
  <head><title>Expression Example</title></head>
  <body>
    <% int y = 10; %>
    <p style="font-size: ${y}"> The font size of this paragraph is ${y}. </p>
  </body>
</html>
```
The above code will instantiate an `int y`, assign the value of 10 to it,
and evaluate `y` twice: once to give the paragraph `font-size: 10`, and again
within the paragraph block. It will generate the following HTML:
```html
<html>
  <head><title>Expression Example </title></head>
  <body>
      <p style="font-size: 10"> The font size of this paragraph is 10. </p>
  </body>
</html>
```
## Directives ##
Directives are commands that are evaluated by the Barbarossa compiler, and will affect the overall page structure.

The syntax of a directive is as follows:
```jsp
<%@ directive %>
```
Here are the valid types of directives:

| Directive Type | Syntax                                      | Description                                                                         |
|----------------|---------------------------------------------|-------------------------------------------------------------------------------------|
| Import         | `<%@ import package %>`                     | Imports Java packages into the generated Java file                                  |
| Extend         | `<%@ extend superclass %>`                  | Extends a superclass in the generated Java file                                     |
| Params         | `<%@ params your-params-here %>`            | Gives parameters for the template                                                   |
| Include        | `<%@ include otherTemplate %>`              | Include a separate template in the template                                         |
| Section        | `<%@ section mySection(optional-params) %>` | Creates a sub-template within the template, that can be called from other templates |
A more detailed explanation of various directive types [can be found
below.](#directive-types)
## Comments ##
Comments are blocks of code that the Barbarossa compiler will ignore. They will **not** be generated as comments in the generated Java code.

The syntax of a comment is as follows:
```jsp
<%-- This is a comment --%>
```
# Directive Keywords #
## Import ##
The `import` keyword is used to import external packages into the generated Java file.

The syntax of the import keyword is as follows:
```jsp
<%@ import [package name] %>
```

For example, you can use the `import` keyword to utilize useful Java packages:
```jsp
<html>
<%@ import java.util.HashSet %>
  <head><title>Import Example</title></head>
  <body>
    <% int y = 10;
       HashSet<Integer> myHashSet = new HashSet<>();
       myHashSet.add(y);
       myHashSet.add(15);
       for(Integer a: myHashSet) { %>
         <p> myHashSet contains ${a}. </p>
       <% } %>
  </body>
</html>
```
The above code will result in the following HTML. Note that the import statements
was necessary to be able to use `java.util.HashSet`.
```html
<html>
  <head><title>Import Example</title></head>
  <body>
    <p> myHashSet contains 10. </p>
    <p> myHashSet contains 15. </p
  </body>
</html>
```

**Note** that the Barbarossa compiler will not check for the validity or necessity
of given import statements; thus, invalid imports may result in compiler errors.

Additionally, note that the location of import statements within the template file
is irrelevant. Although it is idiomatic to include all imports at the beginning of the file,
imports can be placed anywhere and will not affect the generated file.
## extends ##
The `extends` keyword is used to extend a superclass in the generated Java file.
## Include ##
The `include` keyword allows users to insert other templates inside of the given template.

The syntax of the include keyword is as follows:
```jsp
<$@ include [template to include] %>
```

For example, consider the following template, `myTemplate.bb.html`:
```jsp
<% int fontSize = 0; %>
<html>
    <head><title>WHILE LOOP Example</title></head>
    <body>
        <%while ( fontSize <= 3){ %>
            <font color = "green" size = "<%= fontSize %>">
                JSP Tutorial
            </font><br />
            <%fontSize++;%>
        <%}%>
    </body>
</html>
```
We can then include it from another template as such:
```jsp
<%@ include myTemplate %>
```

Both statements will result in the following HTML code:
```html
<html>
    <head><title>WHILE LOOP Example</title></head>
    <body>
            <font color = "green" size = "0">
                JSP Tutorial
            </font><
            <font color = "green" size = "1">
                JSP Tutorial
            </font><br />
            <font color = "green" size = "2">
                JSP Tutorial
            </font><br />
            <font color = "green" size = "3">
                JSP Tutorial
            </font><br />  
    </body>
</html>

```
## Params ##
The `params` keyword is used to give parameters to a template. This is generally
only useful when creating templates that are meant to be included in other templates.

The syntax of the `params` command is as follows:
```jsp
<%@ params(your-params-here) %>
```

For example, I can create the template `createName.bb.html` as the following:

```jsp
<%@ params(String name) %>
<p>Your name is: ${myName}</p>
```

I can then include it in another template as follows:

```jsp
<html>
    <head><title>PARAMS Example</title></head>
    <body>
      <%@ include createName("Sally") %>
      <%@ include createName("Carson") %>
      <%@ include createName("Edward") %>
      <%@ include createName("Harika") %>
    </body>
</html>
```

Then, the following HTML will be generated:
```html
<html>
    <head><title>PARAMS Example</title></head>
    <body>
      <p>Your name is: Sally </p>
      <p>Your name is: Carson </p>
      <p>Your name is: Edward </p>
      <p>Your name is: Harika </p>
    </body>
</html>
```
## Section ##
The `section` keyword will create a subsection of the current template that can
then be added via an `include` keyword in other templates.

The syntax of a `section` block are as follows:
```jsp
  <%@ section sectionName[(optional-parameters)] %>
    SECTION CONTENT HERE
  <%@ end section %>
```
Note that the corresponding `<%@ end section %>` directive must be used - a failure
to do so will result in an error during code generation.

Imports within sections are valid, and will be handled accordingly.

For example, I can create the template `nestedImport.bb.html` as the following:

```jsp
    <h1>This will make sure that nested imports are handled correctly.</h1>
    <%@ section mySection %>
        <%@ import java.util.* %>
        <% HashSet<Integer> myHashSet = new HashSet<>();
        myHashSet.add(1);
        myHashSet.add(2);
        myHashSet.add(3);
        for(Integer a: myHashSet) { %>
        <h2 style="font-size: ${a}">Font size: ${a}</h2>
        <% } %>
    <%@ end section %>
        <p> The above section should work </p>
```

The above code will generate the following HTML:
```html
<h1>This will make sure that nested imports are handled correctly.</h1>
<h2 style="font-size: 1">Font size: 1</h2>
<h2 style="font-size: 2">Font size: 2</h2>  
<h2 style="font-size: 3">Font size: 3</h2>
<p> The above section should work </p>
```

Then, I can include `mySection` it in a separate template:
```jsp
<%@ include nestedImport.mySection %>
```

Which will result in the following HTML:
```html
<h2 style="font-size: 1">Font size: 1</h2>
<h2 style="font-size: 2">Font size: 2</h2>
<h2 style="font-size: 3">Font size: 3</h2>
```

![](https://kek.gg/i/372rfd.png)

Bare Bones Templates (BB Templates) is a lightweight & type safe templating technology for the JVM.
It is modeled loosely on Java Server Pages (JSP), but is divorced from the Servlet API and thus can be
used in any application environment.

BB Templates supports type safe arguments to templates, type safe inclusion of other templates,
shared layouts for templates and custom base classes for application-specific logic, among other features.

BB templates have the suffix `bb` in their name, often optionally followed by the language that the
template is targeting (e.g. `index.bb.html`).

# Table of Contents

- [Basic Syntax](#basic-syntax)
  * [Statements](#statements)
  * [Expressions](#expressions)
  * [Directives](#directives)
  * [Comments](#comments)
- [Directive Keywords](#directive-keywords)
  * [`import`](#import)
  * [`extends`](#extends)
  * [`include`](#include)
  * [`params`](#params)
  * [`section`](#section)
- [Layouts](#layouts)

# Basic Syntax #

As with JSPs, BB Templates consist of regular textual content with various scriptlets and
directives interspersed in that content.

## Statements ##

Statements are similar to JSP scriptlets: they can contain any number of Java
language statements, including variable or method declarations.

The syntax of a statement is as follows:
```jsp
<% code fragment %>
```

Note that any text within a statement *must be valid code in Java*. For example, the statement

```jsp
<% System.out.println("Hello") %>
```
will result in the following Java code being generated:
```java
System.out.println("Hello")
```
which will result in a compiler error, since there is no semicolon to end the line.

## Expressions ##

Expressions are similar to JSP expressions. As explained in [this JSP guide:](https://www.tutorialspoint.com/jsp/jsp_syntax.htm)
>A JSP expression element contains a scripting language expression that is evaluated, converted to a String, and
inserted where the expression appears in the JSP file.

>Because the value of an expression is converted to a String, you can use an expression within a line of text, whether or not it is tagged with HTML, in a JSP file.

>The expression element can contain any expression that is valid according to the Java Language Specification but you cannot use a semicolon to end an expression.

The syntax of an expression is as follows:
```jsp
<%= expression %>
```

Additionally, the following shorthand syntax is also valid:
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

## Comments ##
Comments are blocks of code that the Barbarossa compiler will ignore. They will **not** be generated as comments in the generated Java code.

The syntax of a comment is as follows:
```jsp
<%-- This is a comment --%>

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

```
# Directive Keywords #

## `import` ##
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

The location of import statements within the template file is irrelevant. Although it is idiomatic to include all imports
at the beginning of the file, imports can be placed anywhere and will not affect the generated file.

## `extends` ##
The `extends` keyword is used to make a template extend a different base class, which can be used to provide
additional application specific functionality (e.g. Request and Response objects in a web application).

TODO: example

## `include` ##

The `include` keyword allows users to insert other templates inside of the given template in a type
safe manner.

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

TODO: mention parameter passing

## `params` ##

The `params` keyword is used to give parameters to a template. This is generally
only useful when creating templates that are meant to be included in other templates.

The syntax of the `params` command is as follows:
```jsp
<%@ params(your-params-here) %>
```

For example, I can create the template `NameDisplay.bb.html` as the following:

```jsp
<%@ params(String name) %>
<p>Your name is: ${myName}</p>
```

You can then include it in another template as follows:

```jsp
<html>
    <head><title>PARAMS Example</title></head>
    <body>
      <%@ include NameDisplay("Sally") %>
      <%@ include NameDisplay("Carson") %>
      <%@ include NameDisplay("Edward") %>
      <%@ include NameDisplay("Harika") %>
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

## `section` ##

The `section` keyword will create a subsection of the current template that can
then be added via an `include` keyword in other templates.

The syntax of a `section` block are as follows:
```jsp
  <%@ section sectionName[(symbols-used-in-section)] %>
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
# Layouts

TODO: fill in
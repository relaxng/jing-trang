# Formatting #

Indent by two spaces.

Put spaces around binary operators:
```
x = y + z;
if (foo == null) {
  ...
}
```

Opening braces are on the same line as the keyword, and closing braces are on a line by themselves:

```
if (foo()) {
  bar();
  baz();
}
else {
  baz();
  bar();
}
try {
  foo();
}
catch (FooException e) {
}  
```

except for do/while, where the closing brace is on the same line as the while.

```
do {
  foo();
} while (bar);
```

Don't parenthesize return:

```
return foo.bar();
```

One-line if/else/for clauses typically aren't enclosed in braces:

```
if (foo)
  bar();
```

(but you can use braces if you prefer).

# Java version #

Code should take advantage of Java 5 language features such as generics, as well as Java 5 APIs. In particular,

  * use HashMap not Hashtable (Hashtable has the overhead of synchronization)
  * use ArrayList not Vector (Vector has the overhead of synchronization)
  * use Iterator not Enumeration

Originally Jing and Trang were written to use Java 1.1 language features only, and not all code has yet been upgraded.

# Subversion #

Make sure new files get created with the right properties.  In particular, Java files should be created with `svn:eol-style=native`, so that they get checked out with native line endings on all platforms.  You can do this by finding your SVN config file and adding

```
[miscellany]
enable-auto-props = yes

[auto-props]
*.java = svn:eol-style=native
*.xml = svn:eol-style=native
*.rng = svn:eol-style=native
*.xsl = svn:eol-style=native
*.rnc = svn:eol-style=native
```

On Windows Vista, my Subversion config file is in `C:\Users\jjc\AppData\Roaming\Subversion\config`.
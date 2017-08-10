# DIJoe
This is likely the world's worst DI framework. It doesn't have any dependencies though.

On a more serious note, if you're actually going to look this over, or maybe even worse, try it out, be aware of a few things:

 - I have tried to make error messages as obvious as possible.
 - There are no real unit tests, but instead the main method has some sample usage.
 - This is made the way it is because I wanted to learn more about how DI would work in Java. And what better way than to just make one yourself? If I were a TA or something, this might have been used to create an obligatory assignment or something.
 - If you really want to use it, knock yourself out.

That being said, here's a rough overview of what this thing can do, and how it does it.

At your application's root, which is usually the main method, you create a new DIJoe object. It takes no arguments to do so, so knock yourself out. Then you can register classes that needs to be injected with either an implementing/extending class (you don't need to use interfaces, even though that is likely the best choice. This isn't that good of a framework), or a singleton. A singleton will never be closed, since it's supposed to hang around for the length of the application. So be careful with what you put in there.

A singleton can be named or unnamed. Unnamed singletons will get the name "default", and will be overwritten by later singletons called "default".

Here's some example code that shows how you can use this thing:
```java
// It's instantiated like any other objet.
DIJoe diJoe = new DIJoe();

diJoe.bindSingleton(String.class, "Hello, World!", "std.greeting");
diJoe.bindSingleton(String.class, "Hallo, Verda!");
```

We've now bound two singletons of the string type. one is named "std.greeting" and the other implicitly "default". Let's bind a PrintStream too.

```java
diJoe.bindSingleton(PrintStream.class, System.out);
```

How do you use this?

Assume you have this class (and the relevant interface):

```java
// skipping over imports and package declarations

public final class ExampleStringUser implements Greeter {
  String greeting;
  PrintStream printer;
  
  @Inject
  public ExampleStringUser(String greeting, PrintStream printer) {
    this.greeting = greeting;
    this.printer = printer;
  }

  @Override
  public void greet() {
    printer.println(greeting);
  }
}
```

Now, let's say that we need this thing in our application. Normally, we'd do:

```java
Greeter greeter = new ExampleStringUser("Hallo, Verda!", System.out);
greeter.greet();

Notice that we have to set all the parameters manually, and we have to be explicit with what types we want.
Now, using DI, we can just do this instead:

```java
Greeter greeter = diJoe.resolve(Greeter.class);
```

And it will crash immediately because you haven't told it what to do with Greeter types.

If we set it up more though, like this:

```java
greeter.bindClass(Greeter.class, ExampleStringUser.class);
```

Then DIJoe will take a look at the ExampleStringUser class, and look for a constructor with @Inject annotated, and stop at the first one it finds, and store that. If it doesn't find one it will look for a no-args constructor it can use instead. If it can find that, it will happily use that instead. If it can't find either, it will crash and tell you that there were no valid constructors. (You can just add the @Inject annotation to one that suits the purpose.)

When you then call diJoe.resolve(Greeter.class), it will first look for a default singleton. If it can't find any, it will  look for constructors instead.. If it finds one, it will use that to create your object. If there are neither singletons, nor constructors, it will crash and burn loudly, complaining that you didn't tell it what to do.

Now, the constructor that you put the @Inject annotation on, takes two arguments. We just so happen to have bound both of them to singletons. This it will find both the default singletons, and your code will print "Hallo, Verda!". If you wanted to load a different singleton, you can annotate the parameter with @InjectSingleton with the name of your preferred singleton-name. Make sure you spell it correctly.

If we change our code to reflect this:

```java
// skipping over imports and package declarations

public final class ExampleStringUser implements Greeter {
  String greeting;
  PrintStream printer;
  
  @Inject
  public ExampleStringUser(@InjectSingleton("std.greeting") String greeting, PrintStream printer) {
    this.greeting = greeting;
    this.printer = printer;
  }

  @Override
  public void greet() {
    printer.println(greeting);
  }
}
```


And note how it will look up the new singleton. If it cannot find the singleton, it will throw an exception. It will throw different exceptions depending on whether there are no singletons defined, or the one you specified is missing.

And that's about it. That's how you use it. Whenever you need an instance of a class, just call resolve. It doesn't give you any detailed control over how it goes about this, because it's a throwaway project. :)
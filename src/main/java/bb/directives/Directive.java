package bb.directives;

public abstract class Directive {

    abstract void before(Appendable buffer);

    abstract void after(Appendable buffer);

    abstract String eval();

}

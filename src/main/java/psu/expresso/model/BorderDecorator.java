/** Code: Luke Stine
 * A simple decorator that adds a border around the display value.
 */

package psu.expresso.model;

public class BorderDecorator implements DecoratorIF {
    private final DecoratorIF wrapped;

    public BorderDecorator(DecoratorIF wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String displayValue() {
        return "[Border] " + wrapped.displayValue();
    }
}

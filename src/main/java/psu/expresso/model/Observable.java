package psu.expresso.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract base class providing default observable behavior.
 * Using a self‑referential generic pattern ensures that concrete subclasses know their own type.
 *
 * @param <T> the concrete type that extends Observable.
 */
public abstract class Observable<T extends Observable<T>> {

    // Internal list to hold observers.
    private final List<T> observers = new ArrayList<>();

    public enum ErrorCodes{
        SUCCESS,
        NULL_OBSERVER,
        SELF_OBSERVATION,
        DUPLICATE,
        CYCLE_DETECTED,
        NULL_TARGET,
        NOT_FOUND
    }

    /**
     * Adds an observer if it does not cause a cycle.
     * This method is protected so that only subclasses or internal logic use it.
     *
     * @param observer the observer to add
     * @return an ErrorCodes code representing success or an error condition
     */
    protected ErrorCodes addObserver(T observer) {
        if (observer == null) {
            return ErrorCodes.NULL_OBSERVER;
        }
        // Prevent self-observation.
        if (observer == this) {
            return ErrorCodes.SELF_OBSERVATION;
        }
        // Prevent duplicate additions.
        if (observers.contains(observer)) {
            return ErrorCodes.DUPLICATE;
        }
        // Prevent deep cycles.
        if (createsCycle(observer)) {
            return ErrorCodes.CYCLE_DETECTED;
        }
        observers.add(observer);
        return ErrorCodes.SUCCESS;
    }

    /**
     * Deletes the specified observer.
     *
     * @param observer the observer to remove.
     */
    protected void deleteObserver(T observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all observers.
     */
    protected void notifyObservers() {
        for (T observer : observers) {
            notifyObserver(observer);
        }
    }

    /**
     * Notifies a single observer.
     * Subclasses must override this to define how notifications are sent.
     *
     * @param observer the observer to notify.
     */
    protected abstract void notifyObserver(T observer);

    /**
     * Returns an unmodifiable view of the observer list.
     *
     * @return a read-only list of observers.
     */
    protected List<T> getObservers() {
        return Collections.unmodifiableList(observers);
    }

    /**
     * Checks whether adding the given observer would create a cycle.
     *
     * @param potentialObserver the candidate observer.
     * @return true if adding the candidate would create a cycle.
     */
    protected boolean createsCycle(T potentialObserver) {
        return isReachable(potentialObserver, (T)this, new HashSet<>());
    }

    /**
     * Recursively checks whether target is reachable from start.
     *
     * @param start   the starting observable.
     * @param target  the observable we want to see if reached.
     * @param visited a set of visited observables.
     * @return true if target is reachable from start.
     */
    protected boolean isReachable(Observable<?> start, Observable<?> target, Set<Observable<?>> visited) {
        if (start == target) {
            return true;
        }
        if (visited.contains(start)) {
            return false;
        }
        visited.add(start);
        for (Observable<?> obs : start.getObservers()) {
            if (isReachable(obs, target, visited)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method that lets an observer subscribe to a target observable.
     * Instead of doing target.addObserver(observer), you call this.observe(target)
     * from the observer side.
     *
     * @param target the observable that you want to observe.
     * @return the result from target.addObserver(this) as an ErrorCodes.
     */
    public ErrorCodes observe(Observable<? super T> target) {
        // CRTP guarantees that "this" is of type T.
        return target.addObserver((T)this);
    }

    /**
     * Convenience method that lets an observer unsubscribe from a target observable.
     *
     * @param target the observable that you want to stop observing.
     * @return a ErrorCodes indicating success or an error.
     */
    public ErrorCodes unobserve(Observable<? super T> target) {
        if (target == null) {
            return ErrorCodes.NULL_TARGET;
        }
        if (!target.getObservers().contains((T)this)) {
            return ErrorCodes.NOT_FOUND;
        }
        target.deleteObserver((T)this);
        return ErrorCodes.SUCCESS;
    }
}

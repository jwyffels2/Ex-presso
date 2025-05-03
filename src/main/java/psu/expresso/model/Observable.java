package psu.expresso.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A generic observable base. Implements
 * add/delete observer, cycle-check, and notify.
 */
public abstract class Observable<T extends Observable<T>> {
    private final List<T> observers = new ArrayList<>();

    public enum ErrorCodes {
        SUCCESS,
        NULL_OBSERVER,
        SELF_OBSERVATION,
        DUPLICATE,
        CYCLE_DETECTED,
        NULL_TARGET,
        NOT_FOUND
    }

    protected ErrorCodes addObserver(T observer) {
        if (observer == null) return ErrorCodes.NULL_OBSERVER;
        if (observer == this) return ErrorCodes.SELF_OBSERVATION;
        if (observers.contains(observer)) return ErrorCodes.DUPLICATE;
        if (createsCycle(observer)) return ErrorCodes.CYCLE_DETECTED;
        observers.add(observer);
        return ErrorCodes.SUCCESS;
    }

    protected void deleteObserver(T observer) {
        observers.remove(observer);
    }

    /**
     * Clears <em>all</em> observers at once.
     */
    protected void deleteObservers() {
        observers.clear();
    }

    protected void notifyObservers() {
        for (T obs : observers) {
            notifyObserver(obs);
        }
    }

    protected abstract void notifyObserver(T observer);

    protected List<T> getObservers() {
        return Collections.unmodifiableList(observers);
    }

    protected boolean createsCycle(T potentialObserver) {
        return isReachable(potentialObserver, (T)this, new HashSet<>());
    }

    private boolean isReachable(Observable<?> start,
                                Observable<?> target,
                                Set<Observable<?>> visited)
    {
        if (start == target) return true;
        if (visited.contains(start)) return false;
        visited.add(start);
        for (Observable<?> obs : start.getObservers()) {
            if (isReachable(obs, target, visited)) return true;
        }
        return false;
    }

    public ErrorCodes observe(Observable<? super T> target) {
        return target.addObserver((T)this);
    }

    public ErrorCodes unobserve(Observable<? super T> target) {
        if (target == null) return ErrorCodes.NULL_TARGET;
        if (!target.getObservers().contains((T)this)) return ErrorCodes.NOT_FOUND;
        target.deleteObserver((T)this);
        return ErrorCodes.SUCCESS;
    }
}

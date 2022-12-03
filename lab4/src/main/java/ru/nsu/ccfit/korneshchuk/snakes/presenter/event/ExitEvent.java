package ru.nsu.ccfit.korneshchuk.snakes.presenter.event;


public class ExitEvent extends UserEvent {
    public ExitEvent() {
        super(UserEventType.EXIT);
    }
}

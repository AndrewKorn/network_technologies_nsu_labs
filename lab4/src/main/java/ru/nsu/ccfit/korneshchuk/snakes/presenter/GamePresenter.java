package ru.nsu.ccfit.korneshchuk.snakes.presenter;

import org.jetbrains.annotations.NotNull;
import ru.nsu.ccfit.korneshchuk.snakes.presenter.event.UserEvent;


public interface GamePresenter {
    void fireEvent(@NotNull UserEvent userEvent);
}

package com.plactions.patterns.behavioral;

import com.plactions.model.Player;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ActionQueue {
    private final Queue<ActionCommand> availableActions = new LinkedList<>();
    private final Deque<ActionCommand> executedHistory = new ArrayDeque<>();

    public void setCommands(List<ActionCommand> commands) {
        availableActions.clear();
        executedHistory.clear();
        availableActions.addAll(commands);
    }

    public boolean executeNext(Player player) {
        if (availableActions.isEmpty()) {
            return false;
        }
        ActionCommand cmd = availableActions.poll();
        cmd.execute(player);
        executedHistory.push(cmd);
        return true;
    }

    public boolean undoLast(Player player) {
        if (executedHistory.isEmpty()) {
            return false;
        }
        ActionCommand last = executedHistory.pop();
        last.undo(player);
        // Возвращаем команду обратно в начало очереди
        LinkedList<ActionCommand> temp = new LinkedList<>(availableActions);
        temp.addFirst(last);
        availableActions.clear();
        availableActions.addAll(temp);
        return true;
    }

    public boolean isEmpty() {
        return availableActions.isEmpty();
    }

    public List<ActionCommand> getRemainingCommands() {
        return List.copyOf(availableActions);
    }
}
package top.soyask.calendarii.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mxf on 2018/4/24.
 */

public class PendingAction {

    private List<Action> mActions = new ArrayList<>();

    public void addAction(Action action){
        mActions.add(action);
    }

    public synchronized void execute(){
        for (Action action : mActions) {
            action.execute();
        }
        mActions.clear();
    }

    public interface Action{
        void execute();
    }
}

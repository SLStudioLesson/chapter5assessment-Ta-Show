package com.taskapp.logic;

import java.time.LocalDate;
import java.util.List;

import com.taskapp.dataaccess.LogDataAccess;
import com.taskapp.dataaccess.TaskDataAccess;
import com.taskapp.dataaccess.UserDataAccess;
import com.taskapp.exception.AppException;
import com.taskapp.model.Log;
import com.taskapp.model.Task;
import com.taskapp.model.User;

public class TaskLogic {
    private final TaskDataAccess taskDataAccess;
    private final LogDataAccess logDataAccess;
    private final UserDataAccess userDataAccess;


    public TaskLogic() {
        taskDataAccess = new TaskDataAccess();
        logDataAccess = new LogDataAccess();
        userDataAccess = new UserDataAccess();
    }

    /**
     * 自動採点用に必要なコンストラクタのため、皆さんはこのコンストラクタを利用・削除はしないでください
     * @param taskDataAccess
     * @param logDataAccess
     * @param userDataAccess
     */
    public TaskLogic(TaskDataAccess taskDataAccess, LogDataAccess logDataAccess, UserDataAccess userDataAccess) {
        this.taskDataAccess = taskDataAccess;
        this.logDataAccess = logDataAccess;
        this.userDataAccess = userDataAccess;
    }

    /**
     * 全てのタスクを表示します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findAll()
     * @param loginUser ログインユーザー
     */
    public void showAll(User loginUser) {

        List<Task> tasks = taskDataAccess.findAll();

        tasks.stream()
        .forEach(task -> {
            //ステータス変換
            String status = "";
                if (task.getStatus() == 0) {
                    status = "未着手";
                } else if  (task.getStatus() == 1) {
                    status = "着手中";
                } else if (task.getStatus() == 2) {
                    status = "完了";
                }

            // ユーザー名をそれぞれ表示する
            String userName = "あなた";
            if (task.getRepUser().getCode() != loginUser.getCode()) {
                userName = task.getRepUser().getName();
            }

            // 表示
            System.out.println("タスク名：" + task.getName() + ", 担当者名：" + userName + "が担当しています, ステータス；" + status);
        });
    }

    /**
     * 新しいタスクを保存します。
     *
     * @see com.taskapp.dataaccess.UserDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#save(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code タスクコード
     * @param name タスク名
     * @param repUserCode 担当ユーザーコード
     * @param loginUser ログインユーザー
     * @throws AppException ユーザーコードが存在しない場合にスローされます
     */
    public void save(int code, String name, int repUserCode,User loginUser) throws AppException {
        User user = userDataAccess.findByCode(repUserCode);

        if (user == null) {
            throw new AppException("存在するユーザーコードを入力してください");
        }

        // タスクの登録
        Task task = new Task(code, name, 0, user);
        // データを1件新規登録する
        taskDataAccess.save(task);

        // ログの登録
        Log log = new Log(code, loginUser.getCode(), 0, LocalDate.now());
        // log.csvにデータを1件新規登録
        logDataAccess.save(log);
        System.out.println(task.getName() + "の登録が完了しました。");
            
    }

    /**
     * タスクのステータスを変更します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#update(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code タスクコード
     * @param status 新しいステータス
     * @param loginUser ログインユーザー
     * @throws AppException タスクコードが存在しない、またはステータスが前のステータスより1つ先でない場合にスローされます
     */
    public void changeStatus(int code, int status, User loginUser) throws AppException {
        Task task = taskDataAccess.findByCode(code);
        if (task == null ) {
            throw new AppException("存在するタスクコードを入力してください");
        }

        if (status - task.getStatus() != 1) {
            throw new AppException("ステータスは、前のステータスより1つ先のもののみを選択してください");
        }

        task.setStatus(status);
        taskDataAccess.update(task);
        Log log = new Log(code, loginUser.getCode(), task.getStatus(), LocalDate.now());
        logDataAccess.save(log);
    }
    
    /**
     * タスクを削除します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#delete(int)
     * @see com.taskapp.dataaccess.LogDataAccess#deleteByTaskCode(int)
     * @param code タスクコード
     * @throws AppException タスクコードが存在しない、またはタスクのステータスが完了でない場合にスローされます
     */
    // public void delete(int code) throws AppException {
    // }
}
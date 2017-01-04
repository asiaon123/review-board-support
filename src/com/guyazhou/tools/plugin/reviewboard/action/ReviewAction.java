package com.guyazhou.tools.plugin.reviewboard.action;

import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.tools.plugin.reviewboard.vcs.VCSBuilder;
import com.guyazhou.tools.plugin.reviewboard.service.VCSBuilderFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.InvokeAfterUpdateMode;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.api.Repository;

/**
 * Review action
 * Created by Yakov on 2016/12/26.
 */
public class ReviewAction extends AnAction {

    String changeMessage;

    @Override
    public void actionPerformed(AnActionEvent e) {

        // 要不要final修饰??
        Project project = e.getData(PlatformDataKeys.PROJECT);

        // 获取选择的文件列表
        // 要不要final修饰??
        VirtualFile[] virtualFiles = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (null == virtualFiles || 0 == virtualFiles.length) {
            Messages.showMessageDialog("没有文件需要review", "提醒", Messages.getWarningIcon());
            return;
        }

        // 获取文件所在VCS
        final AbstractVcs abstractVcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(virtualFiles[0]);
        System.out.println(abstractVcs.getName());  // svn, Git
        // 检测是否所有更改的文件都在该VCS下
        if (!ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(abstractVcs, virtualFiles)) {
            // TODO set action disabled
            Messages.showWarningDialog("有些文件不在VCS管理下", "警告");
        }

        // 当前项目文件变更列表管理器
        final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        // 处理选取的文件
        LocalChangeList localChangeList = null;
        for (VirtualFile virtualFile : virtualFiles) {
            localChangeList = changeListManager.getChangeList(virtualFile);
            if (null != localChangeList) {
                System.out.println(localChangeList.getName());
                changeMessage = localChangeList.getName();
                break;
            }
        }

        changeListManager.invokeAfterUpdate(new Runnable() {

            @Override
            public void run() {
                System.out.println("Executing...");
                try {
                    VCSBuilder vcsBuilder = VCSBuilderFactory.getVCSBuilder(abstractVcs);
                    if (null != vcsBuilder) {
                        execute(project, vcsBuilder, virtualFiles);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, InvokeAfterUpdateMode.SYNCHRONOUS_CANCELLABLE, "刷新VCS", ModalityState.current());


    }

    private void execute(final Project project, final VCSBuilder vcsBuilder, VirtualFile[] virtualFiles) throws Exception {
        vcsBuilder.build(project, virtualFiles);
        String diff = vcsBuilder.getDiff();
        if (null == diff) {
            Messages.showMessageDialog(project, "No diff generated", "Warning", null);
            return;
        }

        ReviewBoardClient reviewBoardClient = new ReviewBoardClient();

        Task.Backgroundable task = new Task.Backgroundable(project, "", false, new PerformInBackgroundOption() {
            @Override
            public boolean shouldStartInBackground() {
                return false;
            }

            @Override
            public void processSentToBackground() {

            }
        }) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                Repository[] repositories;
                //repositories = reviewBoardClient.getRepositories();
            }
        };

    }

}

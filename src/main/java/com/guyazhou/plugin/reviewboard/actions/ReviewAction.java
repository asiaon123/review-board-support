package com.guyazhou.plugin.reviewboard.actions;

import com.guyazhou.plugin.reviewboard.exceptions.InvalidArgumentException;
import com.guyazhou.plugin.reviewboard.exceptions.InvalidFileException;
import com.guyazhou.plugin.reviewboard.i18n.MessageBundleUtil;
import com.guyazhou.plugin.reviewboard.i18n.MessageProperties;
import com.guyazhou.plugin.reviewboard.tasks.PrepareVcsInfoTask;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Review action
 *
 * @author YaZhou.Gu
 */
public class ReviewAction extends AnAction {

    private final Logger log = LoggerFactory.getLogger(ReviewAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        log.info("=================================================== Submit Review Start ===================================================");

        try {
            Project project = event.getProject();
            if (project == null) {
                throw new InvalidArgumentException(MessageBundleUtil.getBundle().getString(MessageProperties.PROJECT_NULL));
            }

            // FIXME Can not get the deleted file(s), changelist?
            VirtualFile[] virtualFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
            Map<AbstractVcs, List<VirtualFile>> resultVirtualFilesMap = processVirtualFiles(project, virtualFiles);

            for (Map.Entry<AbstractVcs, List<VirtualFile>> abstractVcsListEntry : resultVirtualFilesMap.entrySet()) {
                StringBuilder vfBuilder = new StringBuilder();
                for (VirtualFile virtualFile : abstractVcsListEntry.getValue()) {
                    vfBuilder.append(virtualFile.getName()).append("-").append(virtualFile.getPath()).append(", ");
                }
                vfBuilder.delete(vfBuilder.length() - 2, vfBuilder.length());
                log.info(String.format("Retriving vcs [ %s ] of files [ %s ]", abstractVcsListEntry.getKey().getName(), vfBuilder.toString()));
                ProgressManager.getInstance().run(new PrepareVcsInfoTask(project, abstractVcsListEntry.getKey(), abstractVcsListEntry.getValue()));
            }
        } catch (Exception e) {
            // log.error will cause error in event log, seperate exception to exception handler
            log.warn("Error occured while retriving repositories info", e);
            Messages.showErrorDialog(e.getMessage(), MessageBundleUtil.getBundle().getString(MessageProperties.MESSAGE_TITLE_ERROR));
        }
    }

    private Map<AbstractVcs, List<VirtualFile>> processVirtualFiles(Project project, VirtualFile[] expectedVirtualFiles) {
        Map<AbstractVcs, List<VirtualFile>> resultVirtualFilesMap = new HashMap<>();

        if (null == expectedVirtualFiles || 0 == expectedVirtualFiles.length) {
            throw new InvalidArgumentException("Empty file(s) selected");
        }

        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        for (VirtualFile expectedVirtualFile : expectedVirtualFiles) {
            // Ignore directory
            if (expectedVirtualFile.isDirectory()) {
                continue;
            }

            // Check if the file is in change list
            LocalChangeList localChangeList = changeListManager.getChangeList(expectedVirtualFile);
            if (null == localChangeList) {
                throw new InvalidFileException(String.format("No changelist detected for file: [ %s ] in local changes, " +
                        "Making some changes or adding it to vcs", expectedVirtualFile.getName()));
            }

            //
            AbstractVcs abstractVcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(expectedVirtualFile);
            if (resultVirtualFilesMap.containsKey(abstractVcs)) {
                resultVirtualFilesMap.get(abstractVcs).add(expectedVirtualFile);
            } else {
                List<VirtualFile> virtualFileList = new ArrayList<>();
                virtualFileList.add(expectedVirtualFile);
                resultVirtualFilesMap.put(abstractVcs, virtualFileList);
            }
        }

        // Currently, we support only one vcs one time
        if (resultVirtualFilesMap.entrySet().size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (AbstractVcs abstractVcs : resultVirtualFilesMap.keySet()) {
                if (abstractVcs == null) {
                    sb.append("unversioned,");
                } else {
                    sb.append(abstractVcs.getName()).append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            throw new InvalidFileException(String.format("Multiple vcs [ %s ] found, only one supported one time, please submit seperately", sb.toString()));
        }
        return resultVirtualFilesMap;
    }

}

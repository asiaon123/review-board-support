package com.guyazhou.tools.plugin.reviewboard.actions;

import com.guyazhou.tools.plugin.reviewboard.exceptions.InvalidArgumentException;
import com.guyazhou.tools.plugin.reviewboard.exceptions.InvalidFileException;
import com.guyazhou.tools.plugin.reviewboard.tasks.PrepareVcsInfoTask;
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

    @Override
    public void actionPerformed(AnActionEvent event) {

        Project currentProject = event.getProject();
        if (currentProject == null) {
            throw new IllegalArgumentException("Can not get project");
        }

        // Get the selected files
        // FIXME Can not get the deleted file(s)
        VirtualFile[] virtualFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);

        // Get selected virtual files
        ChangeListManager changeListManager = ChangeListManager.getInstance(currentProject);
        // Process the virtual files
        Map<AbstractVcs, List<VirtualFile>> resultVirtualFilesMap = processVirtualFiles(currentProject, changeListManager, virtualFiles);

        // Retrive vcs info
        for (Map.Entry<AbstractVcs, List<VirtualFile>> abstractVcsListEntry : resultVirtualFilesMap.entrySet()) {
            ProgressManager.getInstance().run(new PrepareVcsInfoTask(currentProject, abstractVcsListEntry.getKey(), abstractVcsListEntry.getValue()));
        }

    }

    private Map<AbstractVcs, List<VirtualFile>> processVirtualFiles(Project project, ChangeListManager changeListManager, VirtualFile[] expectedVirtualFiles) {
        Map<AbstractVcs, List<VirtualFile>> resultVirtualFilesMap = new HashMap<>();

        if (null == expectedVirtualFiles || 0 == expectedVirtualFiles.length) {

            Messages.showWarningDialog("Please select the file(s) you want to review!", "Warning");
            throw new InvalidArgumentException("Not Found");
        }

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
            StringBuilder sb = new StringBuilder("Multiple vcs [ ");
            for (AbstractVcs abstractVcs : resultVirtualFilesMap.keySet()) {
                if (abstractVcs == null) {
                    sb.append("unversioned,");
                } else {
                    sb.append(abstractVcs.getName()).append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(" ] found, only one supported one time, please submit seperately");
            throw new InvalidFileException(sb.toString());
        }
        return resultVirtualFilesMap;
    }

}

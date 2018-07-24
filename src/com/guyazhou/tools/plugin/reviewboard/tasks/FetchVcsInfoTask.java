package com.guyazhou.tools.plugin.reviewboard.tasks;

import com.guyazhou.tools.plugin.reviewboard.model.repository.Repository;
import com.guyazhou.tools.plugin.reviewboard.model.repository.RepositoryResponse;
import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.VcsProvider;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.VcsProviderFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 * @author YaZhou.Gu 2018/7/23
 */
public class FetchVcsInfoTask implements Runnable {

    private Project project;
    private AbstractVcs abstractVcs;
    private List<VirtualFile> toBeSubmitedFiles;

    public FetchVcsInfoTask(Project project, AbstractVcs abstractVcs, List<VirtualFile> toBeSubmitedFiles) {
        this.project = project;
        this.abstractVcs = abstractVcs;
        this.toBeSubmitedFiles = toBeSubmitedFiles;
    }

    @Override
    public void run() {
        try {
            VcsProvider vcsProvider = VcsProviderFactory.getVcsProvider(abstractVcs);
            execute(project, vcsProvider, toBeSubmitedFiles);
        } catch (Exception e) {
            Messages.showErrorDialog(e.getMessage(), "Error");
        }
    }

    private void execute(final Project project, final VcsProvider vcsProvider, List<VirtualFile> virtualFiles) throws Exception {

        try {
            vcsProvider.build(project, virtualFiles);
        } catch (Exception e) {
            throw new Exception("Build VCS error, " + e.getMessage());
        }

        String diff = vcsProvider.getDifferences();
        if (null == diff) {
            throw new Exception("No differences detected!");
        }

        ReviewBoardClient reviewBoardClient;
        try {
            reviewBoardClient = new ReviewBoardClient();
        } catch (Exception e) {
            throw new Exception("ReviewBoardClient init error, " + e.getMessage());
        }

        RepositoryResponse repositoryResponse;
        try {
            repositoryResponse = reviewBoardClient.getRepositories();
        } catch (Exception e) {
            throw new Exception("Get repository response error, " + e.getMessage());
        }
        Repository[] repositories = repositoryResponse.getRepositories();
        if (null != repositories) {
//            this.showPostForm(project, vcsProvider, repositories);
        }

    }

}

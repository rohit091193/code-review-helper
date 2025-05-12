package com.github.rohit091193.codereviewhelper;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Collection;

public class JavaFileCheckinHandler extends CheckinHandler {

    private final CheckinProjectPanel panel;
    private final Project project;

    public JavaFileCheckinHandler(CheckinProjectPanel panel, Project project) {
        this.panel = panel;
        this.project = project;
    }

    @Override
    public ReturnResult beforeCheckin() {
        Collection<VirtualFile> filesToCheck = panel.getVirtualFiles();

        for (VirtualFile vf : filesToCheck) {
            if (vf.getName().endsWith(".java")) {
                try {
                    String content = new String(vf.contentsToByteArray(), vf.getCharset());
                    String aiResult = AIAnalyzer.analyzeCode(content);

                    // Clean markdown-style popup
                    Messages.showInfoMessage(aiResult, "🤖 AI Code Review: " + vf.getName());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return ReturnResult.COMMIT;
    }

    public static class Factory extends CheckinHandlerFactory {
        @Override
        public CheckinHandler createHandler(CheckinProjectPanel panel, CommitContext commitContext) {
            return new JavaFileCheckinHandler(panel, panel.getProject());
        }
    }
}

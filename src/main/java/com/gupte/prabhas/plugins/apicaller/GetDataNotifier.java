package com.gupte.prabhas.plugins.apicaller;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by pgupte on 7/11/2017.
 */
@Extension
public class GetDataNotifier extends Notifier implements SimpleBuildStep {
    private String apiServer;
    private String apiUser;
    private String apiPass;
    private final static Logger logger = Logger.getLogger(GetDataNotifier.class.getName());


    @DataBoundConstructor
    public GetDataNotifier(String apiServer, String apiUser, String apiPass) {
        this.apiServer = apiServer;
        this.apiUser = apiUser;
        this.apiPass = apiPass;
    }

    public GetDataNotifier() {
    }

    public String getApiServer() {
        return apiServer;
    }

    public String getApiUser() {
        return apiUser;
    }

    public String getApiPass() {
        return apiPass;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private String getImageId(AbstractBuild build, BuildListener listener) {
        String imageId = "";

        try {
            EnvVars envVars = build.getEnvironment(listener);
            imageId = envVars.get("IMAGE_ID", "");
            listener.getLogger().println("IMAGE_ID read from EnvVars is " + imageId);
        } catch (Exception e) {
            listener.getLogger().println(e.getMessage());
        } finally {
            return imageId;
        }
    }

    private String getImageId(Run run, TaskListener listener) {
        String imageId = "";

        try {
            EnvVars envVars = run.getEnvironment(listener);
            imageId = envVars.get("IMAGE_ID", "");
            listener.getLogger().println("IMAGE_ID read from EnvVars is " + imageId);
        } catch (Exception e) {
            listener.getLogger().println(e.getMessage());
        } finally {
            return imageId;
        }
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        listener.getLogger().println("API Caller plugin kicked in.");

        Result r = build.getResult();
        String result = r.toString();
        listener.getLogger().println("Build Result = " + result);

        if (result.equals("SUCCESS")) {
            String imageId = getImageId(build, listener);

            if (imageId.equals("")) {
                listener.getLogger().println("Image id not found.");
            } else {
                listener.getLogger().println("Image id found = " + imageId);

                try {
                    APIClient client = new APIClient(apiServer, apiUser, apiPass);
                    String response = client.getImageScanResult(imageId);
                    listener.getLogger().println(response);
                } catch (Exception e) {
                    listener.getLogger().println("Exception while getting data from API: " + e.getMessage());
                    return false;
                }
            }
        } else {
            listener.getLogger().println("Since the build is not successful, will not call API.");
        }

        return true;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        taskListener.getLogger().println("API Caller plugin kicked in.");

        Result r = run.getResult();
        String result = r.toString();
        taskListener.getLogger().println("Build Result = " + result);

        if (result.equals("SUCCESS")) {
            String imageId = getImageId(run, taskListener);
            if (imageId.equals("")) {
                taskListener.getLogger().println("Image id not found");
            } else {
                taskListener.getLogger().println("Image id found = " + imageId);

                try {
                    APIClient client = new APIClient(apiServer, apiUser, apiPass);
                    String response = client.getImageScanResult(imageId);
                    taskListener.getLogger().println(response);
                } catch (Exception e) {
                    taskListener.getLogger().println("Exception while performing post build task: " + e.getMessage());
                    return;
                }
            }

        }
        return;

    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String apiServer = "https://api.example.com";
        private String apiUser = "api_user";
        private String apiPass = "api_pass";

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Get Data From API";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            this.apiServer = formData.getString("apiServer");
            this.apiUser = formData.getString("apiUser");
            this.apiPass = formData.getString("apiPass");
            save();
            return super.configure(req, formData);
        }

        public FormValidation doCheckApiServer(@QueryParameter String apiServer) {
            try {
                if (!apiServer.equals((String) "https://api.example.com")) {
                    return FormValidation.error("Server name is not valid!");
                } else {
                    return FormValidation.ok();
                }
            } catch (Exception e) {
                return FormValidation.error(e.getMessage());
            }
        }

        public FormValidation doCheckApiUser(@QueryParameter String apiUser) {
            try {
                if (apiUser.trim().equals("")) {
                    return FormValidation.error("API Username cannot be empty.");
                } else {
                    return FormValidation.ok();
                }
            } catch (Exception e) {
                return FormValidation.error(e.getMessage());
            }
        }

        public FormValidation doCheckApiPass(@QueryParameter String apiPass) {
            try {
                if (apiPass.trim().equals("")) {
                    return FormValidation.error("API Password cannot be empty.");
                } else {
                    return FormValidation.ok();
                }
            } catch (Exception e) {
                return FormValidation.error(e.getMessage());
            }
        }

        public FormValidation doCheckConnection(@QueryParameter String apiServer, @QueryParameter String apiUser, @QueryParameter String apiPass) {
            /* TODO: Move this code to testConnection() in client class. */
            try {
                APIClient client = new APIClient(apiServer, apiUser, apiPass);
                boolean testSuccessful = client.testConnection();
                if (!testSuccessful) {
                    return FormValidation.error("Connection test failed.");
                } else {
                    return FormValidation.ok("Connection test successful!");
                }
            } catch (Exception e) {
                return FormValidation.error(e.getMessage());
            }
        }

        public String getApiServer() {
            return apiServer;
        }

        public String getApiUser() {
            return apiUser;
        }

        public String getApiPass() {
            return apiPass;
        }

    }
}

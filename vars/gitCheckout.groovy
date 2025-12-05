import com.sharedlib.GitCheckout

def call(Map config = [:]) {

    // Validate config map
    def requiredParams = ["MY_GIT_URL", "MY_GIT_REPO_TYPE"]
    requiredParams.each { key ->
        if (!config[key] || config[key].trim() == "") {
            error "❌ GIT: Missing required parameter '${key}'"
        }
    }

    def my_git_repo_type = config.MY_GIT_REPO_TYPE.toLowerCase().trim()
    def my_git_url       = config.MY_GIT_URL.trim()
    def my_git_branch    = config.MY_GIT_BRANCH ?: 'main'
    def my_git_credentials_id = config.MY_GIT_CREDENTIALS_ID ?: null

    if (!(my_git_repo_type in ['private', 'public'])) {
        error "❌ MY_GIT_REPO_TYPE must be 'public' or 'private'. Current: '${my_git_repo_type}'"
    }

    if (my_git_repo_type == "private") {
        if (!my_git_credentials_id || my_git_credentials_id.trim().toLowerCase() == "null") {
            error "❌ MY_GIT_CREDENTIALS_ID is required for private repositories."
        } else { echo "⚡ Private repo detected, git credentials must be supplied." }
    } else { echo "⚡ Public repo detected, git credentials not needed." }

    echo "✔ MY_GIT_URL            = ${my_git_url}"
    echo "✔ MY_GIT_REPO_TYPE      = ${my_git_repo_type}"
    echo "✔ MY_GIT_BRANCH         = ${my_git_branch}"
    echo "✔ MY_GIT_CREDENTIALS_ID = ${my_git_credentials_id}"

    return new GitCheckout(this).gitCheckout(
        MY_GIT_URL: my_git_url,
        MY_GIT_BRANCH: my_git_branch,
        MY_GIT_CREDENTIALS_ID: my_git_credentials_id
    )

   // The above step will return the latestCommitId from src/sharedlib/GitCheckout class
}

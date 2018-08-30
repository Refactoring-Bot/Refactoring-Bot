changeDir(){
	cd "$location"
}
commit(){
 git add -A
 git commit -a -m "$commitMessage"
 git push -u origin "$branchName"
}
pullRequest() {
  hub pull-request -h "$repoOwner":"$branchName" "$commitMessage"
}

changeDir
git checkout -b "$branchName"
commit
pullRequest
git checkout "master"
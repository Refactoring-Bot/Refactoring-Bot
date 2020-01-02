package de.refactoringbot.api.main;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.naming.OperationNotSupportedException;

import de.refactoringbot.model.botissuegroup.BotIssueGroup;
import de.refactoringbot.model.sonarqube.SonarIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringbot.api.github.GithubDataGrabber;
import de.refactoringbot.api.gitlab.GitlabDataGrabber;
import de.refactoringbot.api.sonarqube.SonarQubeDataGrabber;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.AnalysisProvider;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.exceptions.GitLabAPIException;
import de.refactoringbot.model.exceptions.SonarQubeAPIException;
import de.refactoringbot.model.github.pullrequest.GithubCreateRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequests;
import de.refactoringbot.model.github.repository.GithubRepository;
import de.refactoringbot.model.gitlab.pullrequest.GitLabCreateRequest;
import de.refactoringbot.model.gitlab.pullrequest.GitLabPullRequests;
import de.refactoringbot.model.gitlab.repository.GitLabRepository;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.sonarqube.SonarQubeIssues;
import de.refactoringbot.services.github.GithubObjectTranslator;
import de.refactoringbot.services.gitlab.GitlabObjectTranslator;
import de.refactoringbot.services.main.BotService;
import de.refactoringbot.services.sonarqube.SonarQubeObjectTranslator;

/**
 * This class transfers all Rest-Requests to correct APIs and returns all
 * objects as translated bot objects.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class ApiGrabber {

	@Autowired
	GithubDataGrabber githubGrabber;
	@Autowired
	GitlabDataGrabber gitlabGrabber;
	@Autowired
	SonarQubeDataGrabber sonarQubeGrabber;
	@Autowired
	GithubObjectTranslator githubTranslator;
	@Autowired
	GitlabObjectTranslator gitlabTranslator;
	@Autowired
	SonarQubeObjectTranslator sonarQubeTranslator;
	@Autowired
	BotService botController;

	/**
	 * This method gets all requests with all comments from an api translated into a
	 * bot object.
	 * 
	 * @param gitConfig
	 * @return botRequests
	 * @throws URISyntaxException
	 * @throws GitHubAPIException
	 * @throws IOException
	 * @throws GitLabAPIException
	 */
	public BotPullRequests getRequestsWithComments(GitConfiguration gitConfig)
			throws URISyntaxException, GitHubAPIException, IOException, GitLabAPIException {

		BotPullRequests botRequests = null;

		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			GithubPullRequests githubRequests = githubGrabber.getAllPullRequests(gitConfig);
			botRequests = githubTranslator.translateRequests(githubRequests, gitConfig);
			break;
		case gitlab:
			GitLabPullRequests gitlabRequests = gitlabGrabber.getAllPullRequests(gitConfig);
			botRequests = gitlabTranslator.translateRequests(gitlabRequests, gitConfig);
			break;
		}
		return botRequests;
	}

	/**
	 * This method replies to User inside a Pull-Request that belongs to a Bot if
	 * the refactoring was successful.
	 * 
	 * @param request
	 * @param gitConfig
	 * @throws Exception
	 * @throws OperationNotSupportedException
	 */
	public void replyToUserInsideBotRequest(BotPullRequest request, BotPullRequestComment comment,
			GitConfiguration gitConfig) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			githubGrabber.responseToBotComment(githubTranslator.createReplyComment(comment, null), gitConfig,
					request.getRequestNumber());
			break;
		case gitlab:
			gitlabGrabber.respondToUser(gitConfig, request.getRequestNumber(), comment.getDiscussionID(),
					comment.getCommentID(), gitlabTranslator.getReplyComment(null));
			break;
		}
	}

	/**
	 * Reply to comment if refactoring failed.
	 * 
	 * @param request
	 * @param gitConfig
	 * @throws Exception
	 */
	public void replyToUserForFailedRefactoring(BotPullRequest request, BotPullRequestComment comment,
			GitConfiguration gitConfig, String errorMessage) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			githubGrabber.responseToBotComment(githubTranslator.createFailureReply(comment, errorMessage), gitConfig,
					request.getRequestNumber());
			break;
		case gitlab:
			gitlabGrabber.respondToUser(gitConfig, request.getRequestNumber(), comment.getDiscussionID(),
					comment.getCommentID(), errorMessage);
			break;
		}
	}

	/**
	 * Check if Branch exists on repository.
	 * 
	 * @param gitConfig
	 * @throws Exception
	 */
	public void checkBranch(GitConfiguration gitConfig, String branchName) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			githubGrabber.checkBranch(gitConfig, branchName);
			break;
		case gitlab:
			gitlabGrabber.checkBranch(gitConfig, branchName);
			break;
		}
	}

	/**
	 * This method checks the user input and creates a git configuration.
	 * 
	 * @param configuration
	 * @return gitConfig
	 * @throws Exception
	 */
	public GitConfiguration createConfigurationForRepo(GitConfigurationDTO configuration) throws Exception {
		GitConfiguration gitConfig = null;
		// Check analysis service data
		checkAnalysisService(configuration);

		// Pick filehoster
		switch (configuration.getRepoService()) {
		case github:
			GithubRepository githubRepo = githubGrabber.checkRepository(configuration.getRepoName(),
					configuration.getRepoOwner(), configuration.getBotToken(), configuration.getFilehosterApiLink());
			githubGrabber.checkGithubUser(configuration.getBotName(), configuration.getBotToken(),
					configuration.getBotEmail(), configuration.getFilehosterApiLink());

			// Create git configuration and a fork
			gitConfig = githubTranslator.createConfiguration(configuration, githubRepo.getUrl(),
					githubRepo.getHtmlUrl());
			return gitConfig;
		case gitlab:
			GitLabRepository gitlabRepo = gitlabGrabber.checkRepository(configuration.getRepoName(),
					configuration.getRepoOwner(), configuration.getBotToken(), configuration.getFilehosterApiLink());
			gitlabGrabber.checkGitlabUser(configuration.getBotName(), configuration.getBotToken(),
					configuration.getBotEmail(), configuration.getFilehosterApiLink());

			// Create git configuration and a fork
			gitConfig = gitlabTranslator.createConfiguration(configuration, gitlabRepo.getLinks().getSelf(),
					gitlabRepo.getHttpUrlToRepo());
			return gitConfig;
		default:
			throw new Exception("Filehoster " + "'" + configuration.getRepoService() + "' is not supported!");
		}
	}

	/**
	 * This method deletes a repository of a filehoster.
	 * 
	 * @param gitConfig
	 * @throws Exception
	 * @throws OperationNotSupportedException
	 */
	public void deleteRepository(GitConfiguration gitConfig) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			githubGrabber.deleteRepository(gitConfig);
			break;
		case gitlab:
			gitlabGrabber.deleteRepository(gitConfig);
			break;
		}
	}

	/**
	 * This method creates a fork of a repository of a filehoster.
	 * 
	 * @param gitConfig
	 * @throws Exception
	 */
	public GitConfiguration createFork(GitConfiguration gitConfig) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			GithubRepository githubFork = githubGrabber.createFork(gitConfig);
			gitConfig = githubTranslator.addForkDetailsToConfiguration(gitConfig, githubFork.getUrl(),
					githubFork.getHtmlUrl());
			return gitConfig;
		case gitlab:
			GitLabRepository gitlabFork = gitlabGrabber.createFork(gitConfig);
			gitConfig = gitlabTranslator.addForkDetailsToConfiguration(gitConfig, gitlabFork.getLinks().getSelf(),
					gitlabFork.getHttpUrlToRepo());
			return gitConfig;
		default:
			throw new Exception("Filehoster not supported!");
		}
	}

	/**
	 * This method gets all issues of a Project from a analysis service.
	 * TODO: test für diese methode schreiben um zu sehen, in welcher reihenfolge die BotIssues dann stehen
	 * @param gitConfig
	 * @return botIssues
	 * @throws Exception
	 */
	public List<BotIssue> getAnalysisServiceIssues(GitConfiguration gitConfig) throws Exception {
		// Pick service
		switch (gitConfig.getAnalysisService()) {
		case sonarqube:
			// Get issues and translate them
			List<SonarQubeIssues> issues = sonarQubeGrabber.getIssues(gitConfig);
				//TODO: evtl hier die issues priorisieren und danach gruppieren, man muss halt die Liste umschreiben, damit
				//TODO: der Wert des Code-Smells noch mit abgespeichert wird
				for (SonarQubeIssues sqIssue : issues){
						for (SonarIssue issue : sqIssue.getIssues()){
								//issue.setCountChanges(githubGrabber.countCommitsFromHistory(issue, gitConfig));
						}
				}

				//here the sonarqube issues will be sorted
				// this part belongs to the first prioritization of the code-smells
				issues = codeSmellPrioritization(issues);

			List<BotIssue> botIssues = new ArrayList<>();
			for (SonarQubeIssues i : issues) {
				botIssues.addAll(sonarQubeTranslator.translateSonarIssue(i, gitConfig));
			}
			return botIssues;
		default:
			throw new UnsupportedOperationException(
					"Analysis-Service '" + gitConfig.getAnalysisService() + "' is not supported!");
		}
	}

		/**
		 * TODO: von public auf private nach den tests
		 *
		 * This Method sorts the sonarqube issues
		 * it will be the first part of the code-smell prioritization
		 *
		 * @param issues
		 * @return
		 */
		public List<SonarQubeIssues> codeSmellPrioritization(List<SonarQubeIssues> issues){
				//TODO: Methode an codeconventions anpassen

				//erste Schleife durchläuft alle Projekte
				for (SonarQubeIssues sonarQubeIssues : issues){
						//TODO: löschen
						for (SonarIssue issue : sonarQubeIssues.getIssues()){
								System.out.println("issue id: " + issue.getKey() + " Creation date: " + issue.getCreationDate() + " issue count: " + issue.getCountChanges());
						}

						//erstes sortieren nach Datum
						sonarQubeIssues.setIssues(dateSort(sonarQubeIssues));
						//TODO: schauen, ob erstes Objekt in der list zuerst gerefactored wird und dann die liste so hin drehen, dass neuestes zuerst steht
						//TODO: schauen wie Date sortiert

						//TODO: löschen
						for (SonarIssue issue : sonarQubeIssues.getIssues()){
								System.out.println("issue id after dateSort: " + issue.getKey() + " Creation date: " + issue.getCreationDate() + " issue count: " + issue.getCountChanges());
						}
						sonarQubeIssues.setIssues(countSort(sonarQubeIssues));

						//TODO: löschen
						for (SonarIssue issue : sonarQubeIssues.getIssues()){
								System.out.println("issue id after countSort: " + issue.getKey() + " Creation date: " + issue.getCreationDate() + " issue count: " + issue.getCountChanges());
						}
				}

				return issues;
		}

		/**
		 * sort the SonarIssues with its creation date
		 * TODO: warum so implementiert?
		 * TODO: warum nicht nach update date sortieren?
		 * @param sqIssues
		 * @return sortedIssues: the List that is sorted after the date
		 */
		private List<SonarIssue> dateSort(SonarQubeIssues sqIssues){
				Date creationDate;
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+'SSSS");
				//in dieser Liste wird in der Schleife pro durchlauf die aktuellen Befunde gespeichert
				List<SonarIssue> sonarIssues;
				//in der Map wird die Id des Befundes mit dem zugehörigen Datum gespeichert
				Map<String, Date> sonarIssueMap = new HashMap<>();
				//in dieser Liste werden die sortierten SonarIssues gespeichert
				List<SonarIssue> sortedIssues;

				//die aktuellen Befunde werden geholt
				sonarIssues = sqIssues.getIssues();

				//zweite Schleife durchläuft alle Befunde der Projekte
				for (SonarIssue sonarIssue : sonarIssues){
						try {
								//für jeden Befund wird ein Date erzeugt
								creationDate = format.parse(sonarIssue.getCreationDate());
								sonarIssueMap.put(sonarIssue.getKey(), creationDate);

						} catch (ParseException e) {
								e.printStackTrace();
						}
				}
				sonarIssueMap = sortByValue(sonarIssueMap);
				//neue Liste für jedes Projekt
				sortedIssues = new ArrayList<>();

				//schleifen um die sonarIssues Liste in die richtige Reihenfolge zu bringen
				for (Map.Entry<String, Date> entry : sonarIssueMap.entrySet() ){
						for (SonarIssue issue : sonarIssues){
								if (entry.getKey().equals(issue.getKey())){
										sortedIssues.add(issue);
								}
						}
				}
				Collections.reverse(sortedIssues);

			return sortedIssues;
		}

		/**
		 * sort the sonarIssues with its count on changes in commits
		 * @param issues
		 * @return sortedIssues: the List that is sorted after the countChanges
		 */
		private List<SonarIssue> countSort(SonarQubeIssues issues){
				int count;
				List<SonarIssue> issueList = issues.getIssues();
				//in dieser Liste werden die sortierten SonarIssues gespeichert
				List<SonarIssue> sortedIssues = new ArrayList<>();

				sortedIssues = bubbleSort(issueList);

				return sortedIssues;
		}

		/**
		 * sort a list with the bubble sort
		 * @param list
		 * @return list, the sorted list
		 */
		private List<SonarIssue> bubbleSort(List<SonarIssue> list){
				SonarIssue temp;

				for (int i = 0; i < list.size() - 2; i++){
						for (int j = 0; j < list.size() - i - 2; j++){
								if (list.get(j).getCountChanges() < list.get(j + 1).getCountChanges()){
										temp = list.get(j);
										list.set(j, list.get(j + 1));
										list.set(j + 1, temp);
								}
						}
				}

				return list;
		}

		/**
		 * Methode aus https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values TODO: zuletzt besucht am:
		 * sort Maps by value
		 * @param map
		 * @param <K>
		 * @param <V>
		 * @return result: the sorted result
		 */
		private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
				List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
				list.sort(Map.Entry.comparingByValue());

				Map<K, V> result = new LinkedHashMap<>();
				for (Map.Entry<K, V> entry : list) {
						result.put(entry.getKey(), entry.getValue());
				}

				return result;
		}

	/**
	 * This method returns the absolute path of a anaylsis service issue. This is
	 * only necessary for analysis services that only return relative paths for
	 * their issues (e.g. SonarQube). Other anaylsis services should just return the
	 * input path in their case-section.
	 * 
	 * @param gitConfig
	 * @param relativePath
	 * @return absoluteFilePath
	 * @throws IOException
	 */
	public String getAnalysisServiceAbsoluteIssuePath(GitConfiguration gitConfig, String relativePath)
			throws IOException {
		// Pick service
		switch (gitConfig.getAnalysisService()) {
		case sonarqube:
			return sonarQubeTranslator.buildIssuePath(gitConfig, relativePath);
		default:
			return null;
		}
	}

	/**
	 * This method creates a request on a filehoster if the refactoring was
	 * performed with issues from a analysis tool.
	 *
	 * @param issue
	 * @param gitConfig
	 * @param newBranch
	 * @throws Exception
	 */
	public void makeCreateRequestWithAnalysisService(BotIssue issue, GitConfiguration gitConfig, String newBranch)
			throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			// Create PR object
				GithubCreateRequest createRequest = githubTranslator.makeCreateRequestWithAnalysisService(issue, gitConfig,
						newBranch);
			// Create PR on filehoster
			githubGrabber.createRequest(createRequest, gitConfig);
			break;
		case gitlab:
			// Create PR Object
			GitLabCreateRequest gitlabCreateRequest = gitlabTranslator.makeCreateRequestWithAnalysisService(issue,
					gitConfig, newBranch);
			// Create PR on filehoster
			gitlabGrabber.createRequest(gitlabCreateRequest, gitConfig);
			break;
		}
	}

		/**
		 * This method creates a request on a filehoster if the refactoring was
		 * performed with issues from a analysis tool.
		 * This method uses the BotIssueGroup to create Pull-Requests
		 *
		 * @param group
		 * @param gitConfig
		 * @param newBranch
		 * @throws Exception
		 */
		public void makeCreateRequestWithAnalysisService(BotIssueGroup group, GitConfiguration gitConfig, String newBranch)
				throws Exception {
				// Pick filehoster
				switch (gitConfig.getRepoService()) {
				case github:
						// Create PR object
						GithubCreateRequest createRequest = githubTranslator.makeCreateRequestWithAnalysisService(group, gitConfig,
								newBranch);//TODO: hier pullrequests mit den gruppen machen
						// Create PR on filehoster
						githubGrabber.createRequest(createRequest, gitConfig);
						break;
				/*case gitlab: TODO: evtl implementieren
						// Create PR Object
						GitLabCreateRequest gitlabCreateRequest = gitlabTranslator.makeCreateRequestWithAnalysisService(group,
								gitConfig, newBranch);
						// Create PR on filehoster
						gitlabGrabber.createRequest(gitlabCreateRequest, gitConfig);
						break;*/
				}
		}

	/**
	 * This method checks the analysis service data.
	 * 
	 * @param configuration
	 * analysisService
	 * param
	 * analysisServiceProjectKey
	 * 
	 * @throws SonarQubeAPIException
	 * @throws URISyntaxException 
	 */
	private void checkAnalysisService(GitConfigurationDTO configuration) throws SonarQubeAPIException, URISyntaxException {
		// Check if input exists
		if (configuration.getAnalysisService() == null || configuration.getAnalysisServiceProjectKey() == null
				|| configuration.getAnalysisServiceApiLink() == null) {
			return;
		}
		// Pick service
		if (configuration.getAnalysisService().equals(AnalysisProvider.sonarqube)) {
			sonarQubeGrabber.checkSonarData(configuration);
		}
	}
}

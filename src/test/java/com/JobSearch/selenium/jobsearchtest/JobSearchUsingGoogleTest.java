package com.JobSearch.selenium.jobsearchtest;

//import static junit.framework.Assert.assertEquals;
//import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/*
 *  The purpose of this project is to gain experience testing web applications with 
 *  Java and Selenium WebDriver.
 *  
 *  February 2017: Wrote test classes; added data parameterization using junit
 *  Future updates: clean up unused code
 */

@RunWith(Parameterized.class)
public class JobSearchUsingGoogleTest {
	private static String baseUrl = "http://www.google.com";
	private String preferredJobSearchSite;
	private String jobTitle = "Software Tester";
	private static WebDriver driver;
	private static ScreenshotHelper screenshotHelper;
	
	public JobSearchUsingGoogleTest(String preferredJobSearchSite){
		this.preferredJobSearchSite = preferredJobSearchSite;
	}
		 
	@Parameters
	public static Collection<Object[] > data(){
		Object[][] data = new Object[][] {{"Indeed"}, {"Zip"}, {"fakesite"}};
		return Arrays.asList(data);
	}
  
	//@BeforeClass
	@Before
	public void openBrowser() {
	    driver = new ChromeDriver();
	    driver.get(baseUrl);
	    screenshotHelper = new ScreenshotHelper();
	    searchGoogle();
	}
	  
	//@AfterClass
	@After
	public void saveScreenshotAndCloseBrowser() throws IOException {
	    screenshotHelper.saveScreenshot("screenshot.png");
	    driver.quit();
	    System.out.println("Test Completed");
	}
	
	//@Test
	public static void searchGoogle(){
		WebDriverWait myWaitVar = new WebDriverWait(driver,10); 		
		Assert.assertEquals("Page title should be google","Google", driver.getTitle());
		WebElement searchField = driver.findElement(By.name("q"));
		searchField.sendKeys("job search sites");
		searchField.submit();
		myWaitVar.until(ExpectedConditions.titleContains("job search sites"));
		Assert.assertTrue("Page title should now start with job search sites", driver.getTitle().startsWith("job search sites"));
				
	}
	
	
	@Test
	public void findAndClickJobSite(){
		WebDriverWait myWaitVar = new WebDriverWait(driver,5);
		boolean matchFound = false;
		//We want to ignore ads and snippet box so only elements with class=r inside of node with class=srg 		
		List<WebElement> allLinks = driver.findElements(By.xpath("//*[@class=\"srg\"]//*[@class=\"r\"]"));
		String[] linkText = new String[allLinks.size()];
		int i = 0;
		int page = 1;
		
		//Loop through several pages of search results
		while(!matchFound){
						
			//Populate linkText array with link text for each element in allLinks List
			for(WebElement e : allLinks){
				linkText[i] = e.getText();				
				i++;
			}	
		
			System.out.println("Checking Google search for " + preferredJobSearchSite + " link");
			System.out.println("Results " + driver.findElement(By.id("resultStats")).getText() + ":");
			
			//Look through each link for our desired job search engine
			for(String t : linkText){
				//Try for partial match of site name and check we only click link once in case of ad links					
				if(t.contains(preferredJobSearchSite) && !matchFound){  	
					//Must be specific if ads are present with similar links, otherwise might not get home page
					driver.findElement(By.xpath("//*[@class=\"srg\"]//*[contains(text(), '"+preferredJobSearchSite+"')]")).click();
					System.out.println("\n**** Match Found for " + preferredJobSearchSite + " ****");
					Assert.assertTrue("Page matches " +preferredJobSearchSite, driver.getTitle().contains(preferredJobSearchSite));							
					matchFound = true;
					//search for our job title now
					searchJobSite();
				}
				//Print links until match is found
				else if(!matchFound){
					System.out.println("- Match not found: " + t);
				}			
			}
		
			if (!matchFound){
				System.out.println("===> No match found on page " +page+ " for " + preferredJobSearchSite + ".  Check spelling <===\n");	
				if(page > 1){
					//After first page we want to wait until result stats displayed at top (ie Page 2 of about 247,000,000 results (0.89 seconds) then click Next
					myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"resultStats\"][contains(text(), 'Page "+page+"')]")));
					driver.findElement(By.xpath("//*[@id=\"pnnext\"]/span[2]")).click();  //Click Next button
				}
				else{
					//Wait for result stats to display at top then click Next button
					myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"resultStats\"]")));
					driver.findElement(By.xpath("//*[@class=\"pn\"]")).click();
				}
				//increment page count and wait until next page loads
				page++;
				myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"resultStats\"][contains(text(), 'Page "+page+"')]")));
				
				//break out of loop by setting matchFound
				if (page == 5){					
					matchFound = true;
					System.out.println("Job search site not listed in top search results!");
				}
				
				//Reset counter and clear allLinks array for next iteration
				i = 0;
				allLinks.clear();
				allLinks = driver.findElements(By.xpath("//*[@class=\"srg\"]//*[@class=\"r\"]"));
			}			
		}
	}
	
	//@Test
	public void searchJobSite(){
		WebDriverWait myWaitVar = new WebDriverWait(driver,15);
		//this.searchGoogle();
		//this.findAndClickJobSite();
		myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("input")));
		WebElement searchField = driver.findElement(By.tagName("input"));
		System.out.println("We are on page: " + driver.getTitle());
		
		searchField.sendKeys(jobTitle);
		searchField.submit();
		System.out.println("We are on page: " + driver.getTitle());
		Assert.assertTrue("Page matches " + jobTitle, driver.getTitle().contains(jobTitle));
	}
	
	
	
	
	//@Test
	public void clickOnSearchResults(){
		WebDriverWait myWaitVar = new WebDriverWait(driver,10); 
		String siteName;
		
		//Second returned result is ZipRecruiter
		siteName = "ZipRecruiter: Job Search - Millions of Jobs Hiring Near You";	
		
		//Finding search result by expected link text stored in variable
		WebElement searchResult = driver.findElement(By.partialLinkText("Zip"));
		
		searchResult.click();
		System.out.println("Current page: " + driver.getTitle());
		
		//Go back to google results
		driver.navigate().back();
		System.out.println("Current page: " + driver.getTitle());
		
		//Now want to find Indeed link
		siteName = "Indeed: Job Search";
		
		//need to wait until we can see the links again
		myWaitVar.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(siteName)));
		
		//Find, click, and print out page title to console
		searchResult = driver.findElement(By.partialLinkText("Indeed"));
		searchResult.click();
		System.out.println("Current page: " + driver.getTitle());		
	}
	
	 //Class to take a screenshot stored in src directory
	 private static class ScreenshotHelper {
	  
	    public void saveScreenshot(String screenshotFileName) throws IOException {
	      File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
	      FileUtils.copyFile(screenshot, new File(screenshotFileName));
	    }
	  }
	}

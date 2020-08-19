# Import libraries
from selenium.webdriver.common.keys import Keys
from selenium import webdriver
# open-source web-based automation tool
from parsel import Selector
# It extracts data from HTML and XML using Xpath and CSS
import pandas as pd
# Used to create CSV
import time
import sys

# Import data file
import parameters
from Person import Person

# Needed LinkedIn URLs Holder
search_urls_holder = []

# Used for CSV
profile_urls_holder = []
names_holder = []
addresses_holder = []
contact_info_holder = []

# Locating chrome driver path
driver = webdriver.Chrome(parameters.chromeWebDriverPath)


# start a. Establish LinkedIn Session
def establishLinkedInSession(in_email, in_password):
    # start 1. Making a new login session with LinkedIn
    # Login to LinkedIn
    driver.get(parameters.linkedIn_login_page)

    # Find Email Field and send email info.
    email = driver.find_element_by_id('username')
    email.send_keys(in_email)

    time.sleep(3)

    # Find password Field and send password info.
    password = driver.find_element_by_id('password')
    password.send_keys(in_password)

    time.sleep(3)

    # Find Submit Button and Click it.
    logInBtn = driver.find_element_by_xpath('//*[@type="submit"]')
    logInBtn.click()

    time.sleep(5)


# end a.

# start b. Making a new Querying session with Google
def queryGoogle(query, numOfResults):
    driver.get(parameters.googleUrl)

    time.sleep(5)

    # Find Search input field and send search info to it.
    searchingField = driver.find_element_by_name('q')
    searchingField.send_keys(query)

    time.sleep(5)

    # Press Enter key over a textbox
    searchingField.send_keys(Keys.RETURN)
    time.sleep(5)

    # Get n number of result from google
    driver.get(driver.current_url + numOfResults)

    # Get Profiles URLs from Google querying result
    for rapper in driver.find_elements_by_class_name('r'):
        for a in rapper.find_elements_by_xpath('.//a'):
            search_urls_holder.append(a.get_attribute('href'))

    print(search_urls_holder)
    time.sleep(2)


# end b.


# start c. Getting Profile Data
def getProfileInfo(profile_url):
    # Session with LinkedIn profile
    driver.get(profile_url)

    time.sleep(3)

    # Get page Source
    selector = Selector(text=driver.page_source)

    # Get person name
    name = selector.xpath(".//ul[contains(@class,'pv-top-card--list')]/li[contains(@class,'t-24')]/text()").get()
    # Get person address
    address = selector.xpath(".//ul[contains(@class,'pv-top-card--list')]/li[contains(@class,'t-16')]/text()").get()

    time.sleep(4)

    # Find contact info Button and Click it.
    contactInfoBtn = driver.find_element_by_xpath('//*[@data-control-name="contact_see_more"]')
    contactInfoBtn.click()

    time.sleep(4)

    # Update the selector to fetch contact information
    selector = Selector(text=driver.page_source)

    contact_links = selector.css('.pv-contact-info__contact-link *::attr(href)').getall()

    # Outputting (delete spaces)
    p_name = name.strip()
    p_address = address.strip()

    p1 = Person(profile_url, p_name, p_address, contact_links)

    print("url: ", p1.url)
    print("Name: ", p1.name)
    print("Address: ", p1.address)
    print("ContactInfo: ", p1.listToString())

    # Adding Person info to csv data frame list
    profile_urls_holder.append(profile_url)
    names_holder.append(p1.name)
    addresses_holder.append(p1.address)
    contact_info_holder.append(p1.listToString())


# end c.

# start d. Write data to csv file
def saveProfilesData(csvFile, urls, names, addresses, contact_info):
    # Writing data frame that combines lists to csv file
    # dictionary of lists
    dict = {'Url': urls, 'Name': names, 'Address': addresses, 'Contact Info': contact_info}

    df = pd.DataFrame(dict)

    # saving the data frame

    df.to_csv(csvFile, index=False, encoding='utf-8-sig')


# end d

# Calling functions
# 1
establishLinkedInSession(parameters.in_email, parameters.in_password)

# 2
queryGoogle(parameters.google_search_query, "&num=100")

# 3
index = 0
substring = "google.com"
for profile_url in search_urls_holder:
    if index < 5 and substring not in profile_url:
        try:
            getProfileInfo(profile_url)
            time.sleep(10)
            index = index + 1
        except:
            print("Unexpected error:", sys.exc_info()[0])
    else:
        continue
# 4
saveProfilesData(parameters.csv_file_name,
                 profile_urls_holder,
                 names_holder,
                 addresses_holder,
                 contact_info_holder)

driver.quit()
sys.exit()

class Person:
    def __init__(self, url, name, address, contactInfo):
        self.url = url
        self.name = name
        self.address = address
        self.contactInfo = contactInfo

    def listToString(self):
        # initialize an empty string
        links = ""

        # traverse in the string
        for link in self.contactInfo:
            links = links + link + " | "

            # return string
        return links



# liferay-upgrades-utils

## Requirements

#### Java 21
#### Gradle 8
#### Liferay's DXP version: 2025.q1.14-lts (May it be available for other Liferay versions, since those DDMStructure* classes do not change a lot)


## How to run

To execute, you need to use the ``convertTextFieldsToRichText`` command in the gogo shell terminal.

```java
public void convertTextFieldsToRichText();
```

It is possible to add parameters when executing the gogo shell command to define the start and limit of the JournalArticle you would like to convert.
``convertTextFieldsToRichText 0 10000``

```java
convertTextFieldsToRichText(int start, int limit) ;
```

Additionally, you can set the page size with the start and end parameters to determine the number of executions.

```java
public void convertTextFieldsToRichText(int start, int pageSize, int limit);
```

Convert the journalArticle by the primary key. ``convertTextFieldsToRichText 6308551 ``

```java
public void convertTextFieldsToRichText(long articleID);
```

Convert the journalArticle by the groupId. ``convertTextFieldsToRichText "20122" ``

```java
public void convertTextFieldsToRichText(String groupId);
```

Convert the journalArticle by the groupId and folderId. ``convertTextFieldsToRichText "20122" "4817053" ``
```java
public void convertTextFieldsToRichText(String groupId, String folderId);
```
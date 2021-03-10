package com.laioffer.job.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;


//let the jackson library to convert the JSON response to this entity model class

//@JsonIgnoreProperties(ignoreUnknown = true): other fields in the response can be safely ignored,
// without this, will have runtime exception
@JsonIgnoreProperties(ignoreUnknown = true)

//@JsonInclude(JsonInclude.Include.NON_NULL): null fields can be skipped and not included
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item {
    //information we need from the GitHub response
    private String id;
    private String title;
    private String location;
    private String companyLogo;
    private String url;
    private String description;
    //Will learn to extract the keywords from job detail
    //set not list: 去重2， 不需要ordering
    private Set<String> keywords;
    private boolean favorite;

    //getters and one setter for the keywords only

    // 下面是什么意思？？？
    // @JsonProperty("id") indicates the mapping, the extract match is not required, but it's required for
    //multi-word snake case and  and camel case conversions, like company_log to companyLogo.
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    @JsonProperty("company_logo")
    public String getCompanyLogo() {
        return companyLogo;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    //不用加@JsonProperty，因为JSON里没有这个data
    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    public boolean getFavorite() { return favorite; }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return favorite == item.favorite &&
                Objects.equals(id, item.id) &&
                Objects.equals(title, item.title) &&
                Objects.equals(location, item.location) &&
                Objects.equals(companyLogo, item.companyLogo) &&
                Objects.equals(url, item.url) &&
                Objects.equals(description, item.description) &&
                Objects.equals(keywords, item.keywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, location, companyLogo, url, description, keywords, favorite);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", companyLogo='" + companyLogo + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", keywords=" + keywords +
                ", favorite=" + favorite +
                '}';
    }

    public static class Builder {
        //跟上面对应
        private String id;
        private String title;
        private String location;
        private String companyLogo;
        private String url;
        private String description;
        private Set<String> keywords;
        private boolean favorite;

        //这里的语法是什么意思？？
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder companyLogo(String companyLogo) {
            this.companyLogo = companyLogo;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder keywords(Set<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        public Builder favorite(Boolean favorite) {
            this.favorite = favorite;
            return this;
        }
        //这是个constructor？还是method？？
        public Item build() {
            Item item = new Item();
            item.id = id;
            item.title = title;
            item.location = location;
            item.companyLogo = companyLogo;
            item.url = url;
            item.description = description;
            item.keywords = keywords;
            item.favorite = favorite;
            return item;
        }

       /* 将来如何construct 一个 item
       Item item = new Item.Builder()
   .id("item_001")
   .title("California Dream")
   .location("San Francisco")
   .companyLogo("https://abc.com/logo.png")
   .url("https://abc.com")
   .description("Silicon Valley")
   .build();
        */
    }

}


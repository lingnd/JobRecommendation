package com.laioffer.job.external;

//干嘛用的？用来做search用的

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.entity.Item;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public class GitHubClient {
    private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
    private static final String DEFAULT_KEYWORD = "developer";

    public List<Item> search(double lat, double lon, String keyword) {
        if(keyword == null) {
            keyword = DEFAULT_KEYWORD;
        }
        try {
            keyword = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //填充通配符，后面的变量要与URL_TEMPLATE里要一一对应
        String url = String.format(URL_TEMPLATE, keyword, lat, lon);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        // Create a custom response handler
        ResponseHandler<List<Item>> responseHandler = response -> {
            if (response.getStatusLine().getStatusCode() != 200) {
                return Collections.emptyList();
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return Collections.emptyList();
            }
            //return EntityUtils.toString(entity);
            ObjectMapper mapper = new ObjectMapper();

            InputStream inputStream = entity.getContent();
            //这里其实是把String转成了Item[]
            Item[] itemsArray = mapper.readValue(inputStream, Item[].class);
            List<Item> items = Arrays.asList(itemsArray);
            extractKeywords(items);

            //List<Item> items = Arrays.asList(mapper.readValue(entity.getContent(), Item[].class));
            //            extractKeywords(items);
            return items;
            //return Arrays.asList(mapper.readValue(entity.getContent(), Item[].class));
        };
        //拿到handler做一些操作

        try {
            return httpclient.execute(new HttpGet(url), responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return "";
        return Collections.emptyList(); //return的是一个inmodifiable的空串， 不可更改，比较安全，别人不能往里面写东西
        //区别于：return new ArrayList<>();


    }

    private void extractKeywords(List<Item> items) {
        MonkeyLearnClient monkeyLearnClient = new MonkeyLearnClient();

       /*  与下面等同
       List<String> descriptions = new ArrayList<>();
       for (Item item : items) {
         descriptions.add(item.getDescription());
       }
       */

        List<String> descriptions = items.stream()
                .map(Item::getDescription)
                .collect(Collectors.toList());

        List<Set<String>> keywordList = monkeyLearnClient.extract(descriptions);

        for(int i = 0; i < items.size(); i++) {
            //add by Hu zhi
            if (items.size() != keywordList.size() && i > keywordList.size() - 1) {
                items.get(i).setKeywords(new HashSet<>());
                continue;
            }
            //到这为止
            items.get(i).setKeywords(keywordList.get(i));
        }
    }
}


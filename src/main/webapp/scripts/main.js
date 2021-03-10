(function () {   //立即执行函数：不会干扰global scope
    var user_id = '1111';
    var user_fullname = 'John';
    var lng = -122.08;
    var lat = 37.38;

    function init() {  //初始化
        //event监听器，一定要放在前面
        //第一行实现了点back to login
        document.querySelector('#login-form-btn').addEventListener('click', onSessionInvalid);
        document.querySelector('#register-form-btn').addEventListener('click', showRegisterForm);
        //register
        document.querySelector('#register-btn').addEventListener('click', register);
        //log in
        document.querySelector('#login-btn').addEventListener('click', login);

        document.querySelector('#nearby-btn').addEventListener('click', loadNearbyItems);
        document.querySelector('#fav-btn').addEventListener('click', loadFavoriteItems);
        document.querySelector('#recommend-btn').addEventListener('click', loadRecommendedItems);

        validateSession();  //session登录过才有，一定时间以内登录过
    }


    function validateSession() {
        onSessionInvalid();
        var url = './login';
        var req = JSON.stringify({});

        // make AJAX call
        ajax('GET', url, req,     //req为空，看看之前有没有登录过，session在不在，代码在login servelet里看
            // session is still valid
            function(res) {
                var result = JSON.parse(res);

                if (result.status === 'OK') {
                    onSessionValid(result);
                }
            }, function(){
                console.log('login error');
            });
    }


    /**
     * When the session is invalid - before we login
     * we should only show the login form
     */
    function onSessionInvalid() {
        var loginForm = document.querySelector('#login-form');
        var registerForm = document.querySelector('#register-form');
        var itemNav = document.querySelector('#item-nav');
        var itemList = document.querySelector('#item-list');
        var avatar = document.querySelector('#avatar');
        var welcomeMsg = document.querySelector('#welcome-msg');
        var logoutBtn = document.querySelector('#logout-link');

        hideElement(itemNav);
        hideElement(itemList);
        hideElement(avatar);
        hideElement(logoutBtn);
        hideElement(welcomeMsg);
        hideElement(registerForm);

        clearLoginError();
        showElement(loginForm);
        initGeoLocation();
    }

    function clearLoginError() {
        document.querySelector('#login-error').innerHTML = '';
    }

    function hideElement(element) {
        element.style.display = 'none'; //inline属性,隐藏元素的优先级放高
    }

    function showElement(element, style) {   //你可以设置element的style
        var displayStyle = style ? style : 'block';
        element.style.display = displayStyle;
    }

    function showRegisterForm() {
        var loginForm = document.querySelector('#login-form');
        var registerForm = document.querySelector('#register-form');
        var itemNav = document.querySelector('#item-nav');
        var itemList = document.querySelector('#item-list');
        var avatar = document.querySelector('#avatar');
        var welcomeMsg = document.querySelector('#welcome-msg');
        var logoutBtn = document.querySelector('#logout-link');

        hideElement(itemNav);
        hideElement(itemList);
        hideElement(avatar);
        hideElement(logoutBtn);
        hideElement(welcomeMsg);
        hideElement(loginForm);

        clearRegisterResult();
        showElement(registerForm);
    }

    function clearRegisterResult() {
        document.querySelector('#register-result').innerHTML = '';
    }

    function register() {
        //拿到四个用户输入
        var username = document.querySelector('#register-username').value;
        var password = document.querySelector('#register-password').value;
        var firstName = document.querySelector('#register-first-name').value;
        var lastName = document.querySelector('#register-last-name').value;

        //如果有一个没填写
        if (username === "" || password == "" || firstName === "" || lastName === "") {
            showRegisterResult('Please fill in all fields');
            return
        }

        //regular express check 用户填写的用户名合规
        if (username.match(/^[a-z0-9_]+$/) === null) {
            showRegisterResult('Invalid username');
            return
        }
        //md5就是做了hashing，用了一个别人的库hashing，md5是一个global函数，就是HTML文件最下面那个引用部分
        //网络上任何信息都可以被别人inject，也能直到信息要发到哪里去，就获得了你的用户名，密码，为了安全性需要hashing
        //password 存在了amx seecle service上
        password = md5(username + md5(password));

        // The request parameters
        var url = './register';  //前端后端代码都在一个服务器上，用相对路径就可以了
        //发送的内容
        var req = JSON.stringify({
            user_id : username,
            password : password,
            first_name: firstName,
            last_name: lastName,
        });


        ajax('POST', url, req,
            // successful callback
            function(res) {
                var result = JSON.parse(res);

                // successfully logged in
                if (result.result === 'OK') {
                    showRegisterResult('Succesfully registered');
                } else {
                    showRegisterResult('User already existed');
                }
            },

            // error
            function() {
                showRegisterResult('Failed to register');
            });
    }

    function showRegisterResult(registerMessage) {
        document.querySelector('#register-result').innerHTML = registerMessage;
    }

    //发送网络请求，且不阻断主线程：一边等待数据，一边不影响网页其他功能
    function ajax(method, url, data, successCallback, errorCallback) {
        var xhr = new XMLHttpRequest();
        //method对应http 的post get
        xhr.open(method, url, true);

        //在网络请求"成功"完成后，做下面的事情
        xhr.onload = function() {
            if (xhr.status === 200) {
                successCallback(xhr.responseText);
            } else {
                errorCallback();
            }
        };

        //如果整个请求失败
        xhr.onerror = function() {
            console.error("The request couldn't be completed.");
            errorCallback();
        };

        //万一没有放数据在请求上
        if (data === null) {
            xhr.send();
        } else {
            xhr.setRequestHeader("Content-Type",
                "application/json;charset=utf-8");
            xhr.send(data);
        }
    }

    function login() {
        var username = document.querySelector('#username').value;
        var password = document.querySelector('#password').value;
        password = md5(username + md5(password)); //这里一定要与上面register保持一致才能登陆

        // The request parameters
        var url = './login';
        var req = JSON.stringify({
            user_id : username,
            password : password,
        });

        ajax('POST', url, req,
            // successful callback
            function(res) {
                var result = JSON.parse(res);

                // successfully logged in
                if (result.status === 'OK') {
                    onSessionValid(result);
                }
            },

            // error
            function() {
                showLoginError();
            });
    }

    function showLoginError() {
        document.querySelector('#login-error').innerHTML = 'Invalid username or password';
    }

    function onSessionValid(result) {
        user_id = result.user_id;
        user_fullname = result.name;

        var loginForm = document.querySelector('#login-form');
        var registerForm = document.querySelector('#register-form');
        var itemNav = document.querySelector('#item-nav');
        var itemList = document.querySelector('#item-list');
        var avatar = document.querySelector('#avatar');
        var welcomeMsg = document.querySelector('#welcome-msg');
        var logoutBtn = document.querySelector('#logout-link');

        welcomeMsg.innerHTML = 'Welcome, ' + user_fullname;

        showElement(itemNav);
        showElement(itemList);
        showElement(avatar);
        showElement(welcomeMsg);
        showElement(logoutBtn, 'inline-block');
        hideElement(loginForm);
        hideElement(registerForm);

    }

    function showLoadingMessage(msg) {
        var itemList = document.querySelector('#item-list');
        itemList.innerHTML = '<p class="notice"><i class="fa fa-spinner fa-spin"></i> ' +
            msg + '</p>';
    }

    function initGeoLocation() {
        if (navigator.geolocation) {   //允许的情况
            navigator.geolocation.getCurrentPosition(
                onPositionUpdated,
                onLoadPositionFailed, {
                    maximumAge: 60000
                });
            showLoadingMessage('Retrieving your location...');
        } else {     //不允许的情况
            onLoadPositionFailed();
        }
    }

    function onPositionUpdated(position) {
        lat = position.coords.latitude;
        lng = position.coords.longitude;

        loadNearbyItems();
    }

    function onLoadPositionFailed() {
        console.warn('navigator.geolocation is not available');
        getLocationFromIP();
    }

    //强行通过IP拿到位置信息
    function getLocationFromIP() {
        // get location from http://ipinfo.io/json
        var url = 'http://ipinfo.io/json'
        var data = null;

        ajax('GET', url, data, function(res) {
            var result = JSON.parse(res);
            if ('loc' in result) {
                var loc = result.loc.split(',');
                lat = loc[0];
                lng = loc[1];
            } else {
                console.warn('Getting location by IP failed.');
            }

            loadNearbyItems();
        });
    }


    /**
     * API #1 Load the nearby items API end point: [GET]
     * /search?user_id=1111&lat=37.38&lon=-122.08
     */
    function loadNearbyItems() {
        console.log('loadNearbyItems');
        activeBtn('nearby-btn'); //鼠标移过去会高亮

        // The request parameters
        var url = './search';
        var params = 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
        var data = null;

        // display loading message
        showLoadingMessage('Loading nearby items...');

        // make AJAX call
        ajax('GET', url + '?' + params, data,
            // successful callback
            function(res) {
                var items = JSON.parse(res);
                if (!items || items.length === 0) {
                    showWarningMessage('No nearby item.');
                } else {
                    listItems(items);
                }
            },
            // failed callback
            function() {
                showErrorMessage('Cannot load nearby items.');
            }
        );
    }


    /**
     * A helper function that makes a navigation button active
     *
     * @param btnId - The id of the navigation button
     */
    function activeBtn(btnId) {
        var btns = document.querySelectorAll('.main-nav-btn');

        // deactivate all navigation buttons
        for (var i = 0; i < btns.length; i++) {
            btns[i].className = btns[i].className.replace(/\bactive\b/, '');
        }

        // active the one that has id = btnId
        var btn = document.querySelector('#' + btnId);
        btn.className += ' active';
    }

    function showWarningMessage(msg) {
        var itemList = document.querySelector('#item-list');
        itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-triangle"></i> ' +
            msg + '</p>';
    }

    function showErrorMessage(msg) {
        var itemList = document.querySelector('#item-list');
        itemList.innerHTML = '<p class="notice"><i class="fa fa-exclamation-circle"></i> ' +
            msg + '</p>';
    }


    /**
     * List recommendation items base on the data received
     *
     * @param items - An array of item JSON objects
     */
    function listItems(items) {
        var itemList = document.querySelector('#item-list');
        itemList.innerHTML = ''; // clear current results

        for (var i = 0; i < items.length; i++) {
            addItem(itemList, items[i]);
        }
    }

    /**
     * Add a single item to the list
     *
     * @param itemList - The <ul id="item-list"> tag (DOM container)
     * @param item - The item data (JSON object)
     *
     */
    function addItem(itemList, item) {
        var item_id = item.id;

        // create the <li> tag and specify the id and class attributes
        var li = $create('li', {
            id : 'item-' + item_id,
            className : 'item'
        });

        // set the data attribute ex. <li data-item_id="G5vYZ4kxGQVCR"
        // data-favorite="true">
        li.dataset.item_id = item_id;
        li.dataset.favorite = item.favorite;

        // item image
        if (item.company_logo) {
            li.appendChild($create('img', {
                src : item.company_logo
            }));
        } else {
            li.appendChild($create('img', {
                src : 'https://via.placeholder.com/100'
            }));
        }
        // section
        var section = $create('div');

        // title
        var title = $create('a', {
            className : 'item-name',
            href : item.url,
            target : '_blank'
        });
        title.innerHTML = item.title;
        section.appendChild(title);

        // keyword
        var keyword = $create('p', {
            className : 'item-keyword'
        });
        keyword.innerHTML = 'Keyword: ' + item.keywords.join(', ');
        section.appendChild(keyword);

        li.appendChild(section);

        // address
        var address = $create('p', {
            className : 'item-address'
        });

        // ',' => '<br/>', '\"' => ''
        address.innerHTML = item.location.replace(/,/g, '<br/>').replace(/\"/g,
            '');
        li.appendChild(address);

        // favorite link
        var favLink = $create('p', {
            className : 'fav-link'
        });

        favLink.onclick = function() {
            changeFavoriteItem(item);
        };

        favLink.appendChild($create('i', {
            id : 'fav-icon-' + item_id,
            className : item.favorite ? 'fa fa-heart' : 'fa fa-heart-o'
        }));

        li.appendChild(favLink);

        itemList.appendChild(li);
    }

    /**
     * A helper function that creates a DOM element <tag options...>
     * @param tag
     * @param options
     * @returns {Element}
     */
    function $create(tag, options) {
        var element = document.createElement(tag);
        for (var key in options) {
            if (options.hasOwnProperty(key)) {
                element[key] = options[key];
            }
        }
        return element;
    }


    /**
     * API #4 Toggle favorite (or visited) items
     *
     * @param item - The item from the list
     *
     * API end point: [POST]/[DELETE] /history request json data: {
     * user_id: 1111, favorite: item }
     */
    function changeFavoriteItem(item) {
        // check whether this item has been visited or not
        var li = document.querySelector('#item-' + item.id);
        var favIcon = document.querySelector('#fav-icon-' + item.id);
        var favorite = !(li.dataset.favorite === 'true');

        // request parameters
        var url = './history';
        var req = JSON.stringify({
            user_id: user_id,
            favorite: item
        });
        var method = favorite ? 'POST' : 'DELETE';

        ajax(method, url, req,
            // successful callback
            function(res) {
                var result = JSON.parse(res);
                if (result.status === 'OK' || result.result === 'SUCCESS') {
                    li.dataset.favorite = favorite;
                    favIcon.className = favorite ? 'fa fa-heart' : 'fa fa-heart-o';
                }
            });
    }

    /**
     * API #2 Load favorite (or visited) items API end point: [GET]
     * /history?user_id=1111
     */
    function loadFavoriteItems() {
        activeBtn('fav-btn');

        // request parameters
        var url = './history';
        var params = 'user_id=' + user_id;
        var req = JSON.stringify({});

        // display loading message
        showLoadingMessage('Loading favorite items...');

        // make AJAX call
        ajax('GET', url + '?' + params, req, function(res) {
            var items = JSON.parse(res);
            if (!items || items.length === 0) {
                showWarningMessage('No favorite item.');
            } else {
                listItems(items);
            }
        }, function() {
            showErrorMessage('Cannot load favorite items.');
        });
    }

    /**
     * API #3 Load recommended items API end point: [GET]
     * /recommendation?user_id=1111
     */
    function loadRecommendedItems() {
        activeBtn('recommend-btn');

        // request parameters
        var url = './recommendation' + '?' + 'user_id=' + user_id + '&lat=' + lat + '&lon=' + lng;
        var data = null;

        // display loading message
        showLoadingMessage('Loading recommended items...');

        // make AJAX call
        ajax('GET', url, data,
            // successful callback
            function(res) {
                var items = JSON.parse(res);
                if (!items || items.length === 0) {
                    showWarningMessage('No recommended item. Make sure you have favorites.');
                } else {
                    listItems(items);
                }
            },
            // failed callback
            function() {
                showErrorMessage('Cannot load recommended items.');
            }
        );
    }

    init();
})();


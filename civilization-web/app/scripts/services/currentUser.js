(function(module) {

    var USERKEY = "authorization_encoded";

    var currentUser = function (localStorage) {

        var saveUser = function () {
            localStorage.add(USERKEY, profile);
        };

        var removeUser = function () {
            localStorage.remove(USERKEY);
        };

        var initialize = function() {
            var user = {
                username: "",
                password: "",
                authorization_encoded: "",
                get loggedIn() {
                    return this.authorization_encoded ? true : false;
                }
            };

            var localUser = localStorage.get(USERKEY);
            if (localUser) {
                user.username = localUser.username;
                user.password = localUser.password;
                user.authorization_encoded = localUser.authorization_encoded;
            }
            return user;
        };

        var profile = initialize();

        return {
            save: saveUser,
            remove: removeUser,
            profile: profile
        };
    };

    module.factory("currentUser", currentUser);

}(angular.module("civApp")));

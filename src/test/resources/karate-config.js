function fn() {
    var env = karate.env;
    karate.log('karate.env =', env);

    if (!env) {
        env = 'dev';
    }

    var config = {
        env: env,
        baseUrl: 'https://jsonplaceholder.typicode.com'
    };

    if (env === 'dev') {
        config.baseUrl = 'https://jsonplaceholder.typicode.com';
    } else if (env === 'staging') {
        config.baseUrl = 'https://your-staging-url.com';
    }

    karate.configure('connectTimeout', 5000);
    karate.configure('readTimeout', 5000);

    return config;
}
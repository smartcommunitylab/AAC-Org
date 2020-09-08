const env = {
    get: (key) => {
        console.log("ask for " + key)
        let value = env.getFromProcess(key)
        const winValue = env.getFromWindow(key)
        if (winValue != null) {
            //override 
            value = winValue
        }
        console.log("got " + value)
        return value

    },
    getFromProcess: (key) => {
        console.log("ask for env " + key)
        let value = process.env[key]
        console.log("read env " + value)
        return value

    },
    getFromWindow: (key) => {
        console.log("ask for window " + key)
        let value = null;
        if (window.hasOwnProperty(key)) {
            value = window[key]
        }
        console.log("read window " + value)
        return value

    },

}

export default env
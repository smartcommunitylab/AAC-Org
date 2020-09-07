
const saveState = (key, state) => {
    try {
        // console.log("serialize state for " + key)
        const serializedState = JSON.stringify(state);
        localStorage.setItem(key, serializedState);
    } catch {
        // ignore write errors
    }
};

export default (previousState = null, { type, payload }) => {
    // console.log('recieved ' + type);
    if (type === 'ORG_SELECT') {
        //serialize
        saveState('org', payload)

        return payload;
    }
    return previousState;
}
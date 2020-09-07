import * as React from 'react';
import { useListContext, TextField } from 'react-admin';
import Chip from '@material-ui/core/Chip'


const TextArrayList = () => {
    const { ids, data, basePath } = useListContext();
    console.log('debug list context');
    console.dir(ids);
    console.dir(data);
    return <div />
    // if (typeof data === 'undefined' || data === null || data.length === 0) {
    //     return <div />
    // } else {
    //     return (
    //         <>
    //             {ids.map(item => <Chip label={item} key={item} />)}
    //         </>
    //     )
    // }
};

export default TextArrayList;
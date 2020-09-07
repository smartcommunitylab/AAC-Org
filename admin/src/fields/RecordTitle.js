import * as React from "react";



const RecordTitle = ({ resource, record }) => (
    record && (<span> {`${resource} ${record.organization}/${record.id}`} </span>)
);

export default RecordTitle;
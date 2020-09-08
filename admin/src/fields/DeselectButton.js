import * as React from "react";
import { Component } from 'react';
import PropTypes from 'prop-types';
// import { Button, CardActions, CircularProgress, makeStyles } from '@material-ui/core';
import { Button, Loading } from 'react-admin'
import { useNotify, useRedirect } from 'react-admin';
import VisibilityOff from '@material-ui/icons/VisibilityOff';


import { connect } from 'react-redux';
import { orgSelect as orgSelectAction } from '../orgSelectAction'

// export const SelectButton = ({ classes, record, orgSelect, ...rest }) => {

//     console.log("render select")
//     console.dir(orgSelect)

//     const handleSelect = () => {
//         console.log("selected")
//         // console.dir(record)
//         //const org = localStorage.setItem('slug', record.id);
//         orgSelect(record.id)
//     };

//     return (
//         <Button
//             onClick={handleSelect}
//             label="Select"
//             {...rest}
//         >
//             Select
//         </Button>
//     );
// };

const defaultIcon = <VisibilityOff />;

const DeselectButton = ({
    basePath,
    label = "Deselect",
    record,
    icon = defaultIcon,
    orgSelect,
    ...rest

}) => {
    const notify = useNotify();
    const redirect = useRedirect();

    const handleSelect = () => {
        orgSelect(null)
        redirect('/organizations')
    };

    if (!record) return <Loading />;


    return (
        <Button
            onClick={handleSelect}
            label={label}
            {...rest}
        >
            {icon}
        </Button>
    );

};

DeselectButton.propTypes = {
    orgSelect: PropTypes.func.isRequired,
    record: PropTypes.object,
};


export default connect(null, {
    orgSelect: orgSelectAction
})(DeselectButton);


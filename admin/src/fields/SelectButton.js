import * as React from "react";
import { Component } from 'react';
import PropTypes from 'prop-types';
// import { Button, CardActions, CircularProgress, makeStyles } from '@material-ui/core';
import { Button, Loading } from 'react-admin'
import { useNotify, useRedirect } from 'react-admin';
import Visibility from '@material-ui/icons/Visibility';

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

const defaultIcon = <Visibility />;

const SelectButton = ({
    basePath,
    label = "Select",
    record,
    icon = defaultIcon,
    orgSelect,
    ...rest

}) => {
    const notify = useNotify();
    const redirect = useRedirect();

    const handleSelect = () => {
        orgSelect(record)
        redirect('/dashboard/' + record.id)
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

SelectButton.propTypes = {
    orgSelect: PropTypes.func.isRequired,
    record: PropTypes.object,
};

// const mapStateToProps = state => ({ org: state.orgSelect });

export default connect(null, {
    orgSelect: orgSelectAction
})(SelectButton);

// export default SelectButton;
import * as React from "react";
import { Component } from 'react';
import PropTypes from 'prop-types';
import { Button, CardActions, CircularProgress, makeStyles } from '@material-ui/core';
import { useNotify, useRedirect } from 'react-admin';
import ImageEye from '@material-ui/icons/RemoveRedEye';


import { connect } from 'react-redux';
import { orgSelect as orgSelectAction } from './orgSelectAction'

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


class SelectButton extends Component {


    handleSelect = () => {
        console.dir(this.props)
        const notify = useNotify();
        const redirect = useRedirect();
        const { record } = this.props;
        console.log("selected")

        if (record) {
            this.props.orgSelect(record)
        }

        // console.dir(record)
        //const org = localStorage.setItem('slug', record.id);
    };

    render() {
        return (
            <Button
                onClick={this.handleSelect}
                label="Select"
                {...this.props.rest}
            >
                Select
            </Button>
        );
    }
};

SelectButton.propTypes = {
    orgSelect: PropTypes.func.isRequired,
    record: PropTypes.object,
};

const mapStateToProps = state => ({ org: state.orgSelect });

export default connect(null, {
    orgSelect: orgSelectAction
})(SelectButton);

// export default SelectButton;
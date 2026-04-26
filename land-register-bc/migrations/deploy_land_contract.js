const Land = artifacts.require("Land");

module.exports = function(_deployer) {
  deployer.deploy(Land); 
};

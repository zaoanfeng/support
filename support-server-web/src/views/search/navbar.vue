<template>
  <div class="navbar" >
    <div v-if="showSearch" class="left-menu">
      <div>
        <img src="@/assets/logo/logo_blue.png" height="40px" style="margin-right:20px;">
      </div>
      <el-input v-model="search" placeholder="请输入搜索内容" style="width:500px">
        <el-button slot="append" icon="el-icon-search" />
      </el-input>
    </div>
    <div class="right-menu">
      <template v-if="device!=='mobile'">
        <!-- <search class="right-menu-item" /> -->

        <error-log class="errLog-container right-menu-item hover-effect"/>

        <screenfull class="right-menu-item hover-effect"/>

        <el-tooltip :content="$t('navbar.size')" effect="dark" placement="bottom">
          <size-select class="right-menu-item hover-effect"/>
        </el-tooltip>

        <lang-select class="right-menu-item hover-effect"/>

      </template>

      <div v-if="islogin" class="right-menu">
        <el-dropdown class="avatar-container right-menu-item hover-effect" trigger="click">
          <div class="avatar-wrapper">
            <img :src="avatar+'?imageView2/1/w/80/h/80'" class="user-avatar">
            <i class="el-icon-caret-bottom"/>
          </div>
          <el-dropdown-menu slot="dropdown">
            <router-link to="/">
              <el-dropdown-item>
                {{ $t('navbar.dashboard') }}
              </el-dropdown-item>
            </router-link>
            <a target="_blank" @click="manager()">
              <el-dropdown-item>
                {{ $t('navbar.github') }}
              </el-dropdown-item>
            </a>
            <el-dropdown-item divided>
              <span style="display:block;" @click="logout">{{ $t('navbar.logOut') }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <div v-else class="right-menu-item">
        <a target="_blank" class="link-type" style="font-family: cursive;" @click="showDialog = true">登录</a>
      </div>
    </div>
    <el-dialog :title="$t('login.title')" :visible.sync="showDialog">
      <login />
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import ErrorLog from '@/components/ErrorLog'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import LangSelect from '@/components/LangSelect'
import Login from '../login'
// import Search from './index'

export default {
  components: {
    ErrorLog,
    Screenfull,
    SizeSelect,
    LangSelect,
    Login
  },
  data() {
    return {
      showDialog: false,
      islogin: true,
      showSearch: false
      // showSearch : !Search.showSearch
    }
  },
  computed: {
    ...mapGetters([
      'sidebar',
      'name',
      'avatar',
      'device'
    ])
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('toggleSideBar')
    },
    manager() {
      this.$router.push({ path: '/dashboard' })
    },
    logout() {
      this.$store.dispatch('LogOut').then(() => {
        location.reload()// In order to re-instantiate the vue-router object to avoid bugs
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  border-bottom: 1px solid ghostwhite;

  .errLog-container {
    display: inline-block;
    vertical-align: top;
  }

  .left-menu {
    height: 46px;
    line-height: 46px;
    float: left;
    padding-left: 20px;
    display: -webkit-inline-box;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background .3s;

        &:hover {
          background: rgba(0, 0, 0, .025)
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}
</style>
